#!/bin/bash
# ============================================================
# AWS AUTOMATIC SETUP
# Author: Bernardo Silva
# With the support of Professor Diego Passos and Fernanda Passos.
#
# Email: bernardomls2004@hotmail.com
#
# This script automates the installation and configuration of
# a development environment, including OpenMPI and ThermionsApp.
# ============================================================

# Defines the server IP
NFS_SERVER=""

# Log file
LOG_FILE="/tmp/script.log"

# Cluster Identity Script
USER_SCRIPT="/home/ubuntu/cluster.sh"


flag_p=false

# This block of code identifies if the flag p is set.
# If it is set then the script will assume that the machine is set to principal.
# If it is not set the script will assume that the machine is set to non-principal.

# getopts processes the options when executed.
# If the flag is identified it is set to true.
while getopts "ps:" opt; do
    case "$opt" in
        p) flag_p=true ;;
        s) NFS_SERVER="$OPTARG" ;;
        \?) echo "Usage: $0 -s <nfs_server_ip> [-p]"; exit 1 ;;
    esac
done

shift $((OPTIND-1))

if [ -z "$NFS_SERVER" ]; then
    echo "Error: NFS server IP is required."
    echo "Usage: $0 -s <nfs_server_ip> [-p]"
    exit 1
fi

echo "Updating and installing base dependencies..."
# DEPENDENCIES_UPDATE
sudo DEBIAN_FRONTEND=noninteractive apt update -y -qq &>> $LOG_FILE
# DEPENCIES_INSTALL  - START
sudo DEBIAN_FRONTEND=noninteractive apt install -y build-essential wget tar arping unzip &>> $LOG_FILE


if [ "$flag_p" = true ]; then
    echo "Setting up NFS Server on Primary Node..."
    # NFS_SERVER_INSTALL_AND_CONFIG
    sudo DEBIAN_FRONTEND=noninteractive apt install nfs-kernel-server -y &>> $LOG_FILE

    # Create the shared folder if it doesn't exist
    sudo mkdir -p /home/ubuntu/projeto
    sudo chown -R ubuntu:ubuntu /home/ubuntu/projeto
    sudo chmod 777 /home/ubuntu/projeto
    sudo mkdir -p /home/ubuntu/projeto /home/ubuntu/.ssh
    sudo chown -R ubuntu:ubuntu /home/ubuntu/projeto /home/ubuntu/.ssh
    sudo chmod 700 /home/ubuntu/.ssh

    # Configure exports file
    echo "/home/ubuntu/projeto *(rw,no_root_squash,sync)" | sudo tee -a /etc/exports
    echo "/home/ubuntu/.ssh *(rw,no_root_squash,sync)" | sudo tee -a /etc/exports
    # Restart NFS service
    sudo systemctl restart nfs-kernel-server >> $LOG_FILE
    # Export the shared folder
    sudo exportfs -ra >> $LOG_FILE

    # Config SSH Trust
    if [ ! -f "/home/ubuntu/.ssh/id_rsa" ]; then
        ssh-keygen -t rsa -b 4096 -N "" -f /home/ubuntu/.ssh/id_rsa
    fi

    cat /home/ubuntu/.ssh/id_rsa.pub >> /home/ubuntu/.ssh/authorized_keys
    chmod 600 /home/ubuntu/.ssh/authorized_keys
    ssh-keyscan -H ${NFS_SERVER} >> /home/ubuntu/.ssh/known_hosts
    chmod 600 /home/ubuntu/.ssh/known_hosts

    # SSH setting to disable host key checking (insecure) // TODO(IN FUTURE THIS MAY BE CHANGED)
    echo -e "Host *\n    StrictHostKeyChecking no\n    UserKnownHostsFile /dev/null" >> /home/ubuntu/.ssh/config
    chmod 600 /home/ubuntu/.ssh/config


    # ---------------------------------------------------------
    # PRIMARY NODE PAYLOAD - CLUSTER IDENTITY
    # ---------------------------------------------------------
    if [ -f "$USER_SCRIPT" ]; then
                echo "Executing user payload for PRIMARY node..."
                chmod +x "$USER_SCRIPT"
                # Execute synchronously
                bash "$USER_SCRIPT" "primary" "$@" #>> $LOG_FILE 2>&1
            else
              echo "Warning: No user payload script found at $USER_SCRIPT" >> $LOG_FILE
        fi


# ============================================================
# SECONDARY NODE
# ============================================================
else
  echo "Setting up NFS Client on Secondary Node..."

  sudo DEBIAN_FRONTEND=noninteractive apt install nfs-common -y &>> $LOG_FILE
  mkdir -p /home/ubuntu/projeto
  mkdir -p /home/ubuntu/.ssh

  MAX_RETRIES=30
  RETRY_COUNT=0
  MOUNT_SUCCESS=false

  while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if sudo mount "${NFS_SERVER}":/home/ubuntu/projeto /home/ubuntu/projeto 2>/dev/null; then
      sudo mount "${NFS_SERVER}":/home/ubuntu/.ssh /home/ubuntu/.ssh
      MOUNT_SUCCESS=true
      break
    else
      sleep 5
      RETRY_COUNT=$((RETRY_COUNT+1))
    fi
  done

  if [ "$MOUNT_SUCCESS" = false ]; then
    echo "CRITICAL ERROR: Failed to mount NFS."
    exit 1
  fi

# Persist mounts
if ! grep -q "${NFS_SERVER}:/home/ubuntu/projeto" /etc/fstab; then
  echo "${NFS_SERVER}:/home/ubuntu/projeto /home/ubuntu/projeto nfs defaults,_netdev,bg 0 0" | sudo tee -a /etc/fstab
fi

if ! grep -q "${NFS_SERVER}:/home/ubuntu/.ssh" /etc/fstab; then
  echo "${NFS_SERVER}:/home/ubuntu/.ssh /home/ubuntu/.ssh nfs defaults,_netdev,bg 0 0" | sudo tee -a /etc/fstab
fi

sudo chown -R ubuntu:ubuntu /home/ubuntu/projeto
sudo chmod 700 /home/ubuntu/.ssh

#---------------------------------------------------------
# INJECTION POINT: SECONDARY NODE PAYLOAD
# ---------------------------------------------------------
if [ -f "$USER_SCRIPT" ]; then
  echo "Executing user payload for SECONDARY node..."
  chmod +x "$USER_SCRIPT"

  # Execute synchronously
  bash "$USER_SCRIPT" "secondary" "$@" #>> $LOG_FILE 2>&1
fi

fi

# Termination signal for ClustEngine
echo "kill-process"
echo "end"
