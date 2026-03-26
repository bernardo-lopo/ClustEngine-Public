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

# Times log file
TIMES_LOG_FILE="$HOME/times.txt"


MPI_TESTS_ON=true

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

# ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=tmp/tmp_hostfile -o ConnectTimeout=10 -i C:\Users\berna\Desktop\docs\servers-fct.pem -p 2222 ubuntu@localhost chmod +x /home/ubuntu/setup_cluster.sh; /home/ubuntu/setup_cluster.sh -p -s 192.168.1.238 192.168.1.238 192.168.1.169

if [ -z "$NFS_SERVER" ]; then
    echo "Error: NFS server IP is required."
    echo "Usage: $0 -s <nfs_server_ip> [-p]"
    exit 1
fi
echo "Iniciou o download das dependecias" >> $LOG_FILE
echo "Downloading dependencies..." >> $LOG_FILE

# ============================================================
# DEPENDENCIES_UPDATE  - START
# ============================================================

dependencies_update_time_start=$(date +%s%N)

sudo DEBIAN_FRONTEND=noninteractive apt update -y -qq

dependencies_update_time_end=$(date +%s%N)

dependencies_update_result_float=$(awk "BEGIN {printf \"%.9f\", ($dependencies_update_time_end - $dependencies_update_time_start)/1000000000}")
dependencies_update_seconds_int=$(printf "%.0f" "$dependencies_update_result_float")

dependencies_update_minutes=$((dependencies_update_seconds_int / 60))
dependencies_update_seconds=$((dependencies_update_seconds_int % 60))

dependencies_update_output="${dependencies_update_seconds_int}s -> ${dependencies_update_minutes}m ${dependencies_update_seconds}s"

echo "$dependencies_update_output" >> $TIMES_LOG_FILE

# ============================================================
# DEPENCIES_UPDATE  - END
# ============================================================

echo "Finalizou com sucesso o update dos pacotes apt" >> $LOG_FILE

echo "Installing dependencies..." >> $LOG_FILE

# ============================================================
# DEPENCIES_INSTALL  - START
# ============================================================

dependencies_install_time_start=$(date +%s%N)

sudo DEBIAN_FRONTEND=noninteractive apt install -y build-essential wget tar arping unzip

dependencies_install_time_end=$(date +%s%N)

dependencies_install_result_float=$(awk "BEGIN {printf \"%.9f\", ($dependencies_install_time_end - $dependencies_install_time_start)/1000000000}")
dependencies_install_seconds_int=$(printf "%.0f" "$dependencies_install_result_float")

dependencies_install_minutes=$((dependencies_install_seconds_int / 60))
dependencies_install_seconds=$((dependencies_install_seconds_int % 60))

dependencies_install_output="${dependencies_install_seconds_int}s -> ${dependencies_install_minutes}m ${dependencies_install_seconds}s"

echo "$dependencies_install_output" >> $TIMES_LOG_FILE

# ============================================================
# DEPENCIES_INSTALL  - END
# ============================================================

if [ "$flag_p" = true ]; then
    echo "É maquina principal e iniciou as configurações do servidor NFS" >> $LOG_FILE
    echo "Flag P set" >> $LOG_FILE

    # ============================================================
    # NFS_SERVER_INSTALL_AND_CONFIG  - START
    # ============================================================

    nfs_server_install_and_config_time_start=$(date +%s%N)

    sudo DEBIAN_FRONTEND=noninteractive apt install nfs-kernel-server -y

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

    echo "É maquina principal e reiniciou o servidor NFS" >> $LOG_FILE

    # Restart NFS service
    sudo systemctl restart nfs-kernel-server

    # Export the shared folder
    sudo exportfs -ra

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

    nfs_server_install_and_config_time_end=$(date +%s%N)
    nfs_server_install_and_config_result_float=$(awk "BEGIN {printf \"%.9f\", ($nfs_server_install_and_config_time_end - $nfs_server_install_and_config_time_start)/1000000000}")
    nfs_server_install_and_config_seconds_int=$(printf "%.0f" "$nfs_server_install_and_config_result_float")

    nfs_server_install_and_config_minutes=$((nfs_server_install_and_config_seconds_int / 60))
    nfs_server_install_and_config_seconds=$((nfs_server_install_and_config_seconds_int % 60))

    nfs_server_install_and_config_output="${nfs_server_install_and_config_seconds_int}s -> ${nfs_server_install_and_config_minutes}m ${nfs_server_install_and_config_seconds}s"

    echo "$nfs_server_install_and_config_output" >> $TIMES_LOG_FILE

    # ============================================================
    # NFS_SERVER_INSTALL_AND_CONFIG - END
    # ============================================================

    echo "É maquina principal e iniciou o download do MPI" >> $LOG_FILE
    cd /tmp

    if $MPI_TESTS_ON ; then
      echo "Downloading MPI from open-mpi.org..." >> $LOG_FILE

      # ============================================================
      # MPI_DOWNLOAD - START
      # ============================================================

      mpi_download_time_start=$(date +%s%N)

      # wget https://download.open-mpi.org/release/open-mpi/v5.0/openmpi-5.0.7.tar.gz &>> $HOME/erros.txt
      wget https://download.open-mpi.org/release/open-mpi/v4.0/openmpi-4.0.1.tar.gz &>> $HOME/erros.txt

      #tar -xzvf openmpi-5.0.7.tar.gz &>> $HOME/erros.txt
      tar -xzvf openmpi-4.0.1.tar.gz &>> $HOME/erros.txt

      mpi_download_time_end=$(date +%s%N)

      mpi_download_result_float=$(awk "BEGIN {printf \"%.9f\", ($mpi_download_time_end - $mpi_download_time_start)/1000000000}")
      mpi_download_seconds_int=$(printf "%.0f" "$mpi_download_result_float")

      mpi_download_minutes=$((mpi_download_seconds_int / 60))
      mpi_download_seconds=$((mpi_download_seconds_int % 60))

      mpi_download_output="${mpi_download_seconds_int}s -> ${mpi_download_minutes}m ${mpi_download_seconds}s"

      echo "$mpi_download_output" >> $TIMES_LOG_FILE

      # ============================================================
      # MPI_DOWNLOAD - END
      # ============================================================


      # ============================================================
      # MPI_CONFIG_COMPILE - START
      # ============================================================

      mpi_config_compile_time_start=$(date +%s%N)

      #cd openmpi-5.0.7
      cd openmpi-4.0.1
      echo "É maquina principal e finalizou o download do MPI" >> $LOG_FILE
      echo "É maquina principal e iniciou as configurações do mpi" >> $LOG_FILE
      echo "Configuring MPI..." >> $LOG_FILE
      ./configure --prefix=/home/ubuntu/projeto/mpi_inst &>> $HOME/erros.txt
      echo "É maquina principal e iniciou a compilação do mpi" >> $LOG_FILE
      echo "Compiling MPI..." >> $LOG_FILE
      make -j$(nproc) &>> $HOME/erros.txt
      sudo make install &>> $HOME/erros.txt
      echo "É maquina principal e a acabou compilação do mpi" >> $LOG_FILE
      echo "Setting up environment variables..." >> $LOG_FILE
      export PATH=/home/ubuntu/projeto/mpi_inst/bin:$PATH
      export LD_LIBRARY_PATH=/home/ubuntu/projeto/mpi_inst/lib:$LD_LIBRARY_PATH
      sudo ldconfig
      echo "É maquina principal e iniciou o dowload do thermions" >> $LOG_FILE
      echo "Downloading ThermionsApp..." >> $LOG_FILE
      cd /home/ubuntu/projeto
      wget --no-check-certificate "https://drive.google.com/uc?export=download&id=1RHEW2AMwrsRd1IolUkXyHd_gxrZpiXC7" -O ThermionsApp.zip &>> $HOME/erros.txt
      wget 'https://drive.usercontent.google.com/download?id=1pvI_-2ji0MDW1FGodZIOQfB1ia8-cceR&export=download&confirm=t' -O ThermionsApp-AMS.zip
      # if [ -f "ThermionsApp.zip" ]; then
      if [ -f "ThermionsApp-AMS.zip" ]; then
          echo "Extracting ThermionsApp..." >> $LOG_FILE
          unzip ThermionsApp-AMS.zip
          unzip ThermionsApp.zip
      else
          echo "Error: Failed to download ThermionsApp!" >> $LOG_FILE
          exit 1
      fi
      echo "É maquina principal e iniciou a compilação do thermions" >> $LOG_FILE
      echo "Compiling ThermionsApp..." >> $LOG_FILE
      cd ThermionsApp
      mpicc MPIThermions/thermionSD.c -o MPIThermions/thermionSD -lm -O3
      gcc SeqThermions/thermionSeq.c -o SeqThermions/thermionSeq -lm -O3
      cd ..
      echo "É maquina principal e finalizou a compilação do thermions" >> $LOG_FILE
    fi

    mpi_config_compile_time_end=$(date +%s%N)

    mpi_config_compile_result_float=$(awk "BEGIN {printf \"%.9f\", ($mpi_config_compile_time_end - $mpi_config_compile_time_start)/1000000000}")
    mpi_config_compile_seconds_int=$(printf "%.0f" "$mpi_config_compile_result_float")

    mpi_config_compile_minutes=$((mpi_config_compile_seconds_int / 60))
    mpi_config_compile_seconds=$((mpi_config_compile_seconds_int % 60))

    mpi_config_compile_output="${mpi_config_compile_seconds_int}s -> ${mpi_config_compile_minutes}m ${mpi_config_compile_seconds}s"

    echo "$mpi_config_compile_output" >> $TIMES_LOG_FILE

    # ============================================================
    # MPI_CONFIG_COMPILE - END
    # ============================================================

else
    echo "Não é maquina principal e instalou o NFS" >> $LOG_FILE
    echo "Flag not set" >> $LOG_FILE

    # ============================================================
    # NFS_CLIENT_INSTALL - START
    # ============================================================

    nfs_client_install_time_start=$(date +%s%N)

    sudo DEBIAN_FRONTEND=noninteractive apt install nfs-common -y >> $LOG_FILE


    nfs_client_install_time_end=$(date +%s%N)

    nfs_client_install_result_float=$(awk "BEGIN {printf \"%.9f\", ($nfs_client_install_time_end - $nfs_client_install_time_start)/1000000000}")
    nfs_client_install_seconds_int=$(printf "%.0f" "$nfs_client_install_result_float")

    nfs_client_install_minutes=$((nfs_client_install_seconds_int / 60))
    nfs_client_install_seconds=$((nfs_client_install_seconds_int % 60))

    nfs_client_install_output="${nfs_client_install_seconds_int}s -> ${nfs_client_install_minutes}m ${nfs_client_install_seconds}s"

    echo "$nfs_client_install_output" >> $TIMES_LOG_FILE

    # ============================================================
    # NFS_CLIENT_INSTALL - END
    # ============================================================
    
    mkdir -p /home/ubuntu/projeto
        mkdir -p /home/ubuntu/.ssh

        echo "A aguardar pela montagem do NFS (timeout de 10 minutos)..." >> $LOG_FILE

        echo "A aguardar que o servidor principal inicie o NFS..." >> $LOG_FILE

                MAX_RETRIES=30
                RETRY_COUNT=0
                MOUNT_SUCCESS=false

                while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
                    if sudo mount ${NFS_SERVER}:/home/ubuntu/projeto /home/ubuntu/projeto 2>/dev/null; then
                        sudo mount ${NFS_SERVER}:/home/ubuntu/.ssh /home/ubuntu/.ssh
                        MOUNT_SUCCESS=true
                        echo "Montagem bem sucedida à tentativa $((RETRY_COUNT+1))!" >> $LOG_FILE
                        break
                    else
                        echo "Servidor NFS ainda não está pronto. A aguardar 5 segundos..." >> $LOG_FILE
                        sleep 5
                        RETRY_COUNT=$((RETRY_COUNT+1))
                    fi
                done

                if [ "$MOUNT_SUCCESS" = false ]; then
                    echo "ERRO CRITICO: Falha ao montar o NFS após $MAX_RETRIES tentativas." >> $LOG_FILE
                    exit 1
                fi

            # Automatic mount no fstab
           if ! grep -q "${NFS_SERVER}:/home/ubuntu/projeto" /etc/fstab; then
               echo "${NFS_SERVER}:/home/ubuntu/projeto /home/ubuntu/projeto nfs defaults,_netdev,bg 0 0" | sudo tee -a /etc/fstab
           fi

           if ! grep -q "${NFS_SERVER}:/home/ubuntu/.ssh" /etc/fstab; then
               echo "${NFS_SERVER}:/home/ubuntu/.ssh /home/ubuntu/.ssh nfs defaults,_netdev,bg 0 0" | sudo tee -a /etc/fstab
           fi

        echo "Montagens NFS concluídas. A ajustar permissões..." >> $LOG_FILE
        sudo chown -R ubuntu:ubuntu /home/ubuntu/projeto
        sudo chmod 700 /home/ubuntu/.ssh
    fi

echo "Setting up environment variables..." >> $LOG_FILE
export PATH=/home/ubuntu/projeto/mpi_inst/bin:$PATH
export LD_LIBRARY_PATH=/home/ubuntu/projeto/mpi_inst/lib:$LD_LIBRARY_PATH
echo "export PATH=/home/ubuntu/projeto/mpi_inst/bin:\$PATH" >> ~/.bashrc
echo "export LD_LIBRARY_PATH=/home/ubuntu/projeto/mpi_inst/lib:\$LD_LIBRARY_PATH" >> ~/.bashrc
source ~/.bashrc
sudo ldconfig

if $MPI_TESTS_ON ; then
  echo "Não é maquina principal e iniciou a verificação do MPI" >> $LOG_FILE
  # Verifying OpenMPI installation
  echo "Checking OpenMPI installation..." >> $LOG_FILE
  if [ ! -f "/home/ubuntu/projeto/mpi_inst/lib/libpmix.so.2" ]; then
      echo "Error: libpmix.so.2 not found! Installing missing dependencies..." >> $LOG_FILE
      sudo DEBIAN_FRONTEND=noninteractive apt install -y libpmix-dev libhwloc-dev
      sudo ldconfig
  fi

  echo "Checking MPI installation..." >> $LOG_FILE
  mpirun --version

  echo "Creating MPI hosts file..." >> $LOG_FILE
  HOSTS_FILE="/home/ubuntu/projeto/ThermionsApp-AMS/temp-hosts"
  mkdir -p "$(dirname "$HOSTS_FILE")"
   # Clean the file if already exists
  > "$HOSTS_FILE"

  shift $((OPTIND -1))

  for ip in "$@"; do
      echo "$ip slots=1" >> "$HOSTS_FILE"
  done

  echo "Hosts file created at $HOSTS_FILE" >> $LOG_FILE
fi

echo "Installation complete!" >> $LOG_FILE

# This string is being read in the server to actually terminate the process
echo "kill-process"

echo "end"