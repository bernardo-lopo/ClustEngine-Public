
-----------------------------------------------------------------------------
INTRODUCTION
-----------------------------------------------------------------------------
This document indicates the strict rules required to write a custom script for ClustEngine. 
The payload script is injected into the cluster and executed automatically by the base infrastructure script after the network, SSH keys, and NFS mounts are fully established.
-----------------------------------------------------------------------------
ROLE-BASED EXECUTION
-----------------------------------------------------------------------------
This script will be executed on every node in the cluster, but its behavior
must change based on the machine's role.
The base script passes the role as the FIRST argument ($1).

- The script MUST read `$1`.
- If `$1 == "primary"`: The script must execute heavy tasks (compiling,
  downloading source code, setting up shared configurations).
- If `$1 == "secondary"`: The script must only install local dependencies
  (e.g., C libraries, apt packages) required to run the shared software.
-----------------------------------------------------------------------------
CLUSTER IP ADDRESSES (ARGUMENTS)
-----------------------------------------------------------------------------
After the role argument ($1), ClustEngine automatically passes the private
IP addresses of every node in your cluster as the remaining arguments.

- $1 is ALWAYS the role ("primary" or "secondary").
- $2, $3, $4... are the private IP addresses of the instances.
- You can access all of these IPs at once using the bash array `$@` after
  shifting out the first argument (`shift 1`).
-----------------------------------------------------------------------------
STRICTLY NON-INTERACTIVE
-----------------------------------------------------------------------------
ClustEngine runs this script via SSH in the background. If your script stops
to ask the user a question (e.g., "Do you want to continue? [Y/n]"), the
deployment will hang indefinitely and the cluster creation will fail.

- ALWAYS use `DEBIAN_FRONTEND=noninteractive`.
- ALWAYS use `-y` or `-qq` with `apt update` and `apt install`.
- ALWAYS use `--quiet` or redirect output (`&> /dev/null`) for commands like
  `wget`, `tar`, and `unzip`.

-----------------------------------------------------------------------------
THE SHARED DIRECTORY PROMISE
-----------------------------------------------------------------------------
By the time it starts executing, ClustEngine guarantees that a Network File
System (NFS) is fully operational.

- Path: `/home/ubuntu/projeto`
- Rule: Any software, binaries, data sets, or host files that need to be
  accessed by all machines must be placed inside this directory.
- Do not compile software on secondary nodes. It should be compiled once on the primary
  node inside the shared directory. The secondary nodes will instantly have
  access to the compiled binaries.

-----------------------------------------------------------------------------
ENVIRONMENT VARIABLES
-----------------------------------------------------------------------------
If the designated software requires paths to be modified  (e.g., pointing to custom binaries
compiled in the shared folder), the script must inject them into the user's
bash profile.

- Append paths to `/home/ubuntu/.bashrc`.
- Call `source /home/ubuntu/.bashrc` within the script if subsequent
  commands rely on those paths.
- If there are a request for installing shared libraries (.so files), it needs to be run `sudo ldconfig`
  after updating the `LD_LIBRARY_PATH`.

-----------------------------------------------------------------------------
LOGGING AND ERROR HANDLING
-----------------------------------------------------------------------------
To ensure users can debug their custom payloads from the ClustEngine GUI:
- Echo important milestones (e.g., "Compiling MPI...") to `stdout`.
- Redirect heavy, unreadable build logs to a temporary file (e.g.,
  `make -j$(nproc) &>> /tmp/user_build_errors.log`) to avoid flooding the
  ClustEngine real-time terminal.
- If a critical failure occurs, use `exit 1` to immediately halt the execution.

-----------------------------------------------------------------------------
INFRASTRUCTURE MODIFICATIONS
-----------------------------------------------------------------------------
The user payload must not attempt to alter:
- `/etc/fstab` (NFS mounts)
- `/home/ubuntu/.ssh/` (Authorized keys, known hosts, or config)
- `ufw` or `iptables` (Firewall rules)

-----------------------------------------------------------------------------
ClustEngine Cluster Configuration Template
-----------------------------------------------------------------------------

```bash
#!/bin/bash

export DEBIAN_FRONTEND=noninteractive
ROLE=$1
PROJECT_DIR="/home/ubuntu/projeto"

echo "Cluster Configuration: Initializing configuration for role: $ROLE"

if ! grep -q "$PROJECT_DIR/mpi_inst/bin" /home/ubuntu/.bashrc; then
    echo "export PATH=$PROJECT_DIR/mpi_inst/bin:\$PATH" >> /home/ubuntu/.bashrc
    echo "export LD_LIBRARY_PATH=$PROJECT_DIR/mpi_inst/lib:\$LD_LIBRARY_PATH" >> /home/ubuntu/.bashrc
fi
source /home/ubuntu/.bashrc

if [ "$ROLE" == "primary" ]; then
    echo "Configuration: Downloading and compiling source code..."

    cd /tmp
    wget --quiet [https://example.com/source.tar.gz](https://example.com/source.tar.gz)
    tar -xzf source.tar.gz
    cd source
    
    ./configure --prefix=$PROJECT_DIR/my_app &> /tmp/config.log
    make -j$(nproc) &> /tmp/build.log
    sudo make install &>> /tmp/build.log
    sudo ldconfig

elif [ "$ROLE" == "secondary" ]; then
    echo "Configuration: Installing local dependencies..."
    
    sudo apt update -y -qq
    sudo apt install -y -qq libhwloc-dev
    sudo ldconfig
fi

if [ "$ROLE" == "primary" ]; then
    HOSTS_FILE="$PROJECT_DIR/hosts.txt"
    > "$HOSTS_FILE"

    # Shift exactly 1 time to remove the $ROLE argument ("primary")
    shift 1
    
    # Writes clean IPs to the shared hosts file
    # $@ now contains ONLY the IP addresses passed by the base script
    for ip in "$@"; do
        echo "$ip" >> "$HOSTS_FILE"
    done
    
    echo "Configuration: Hosts file generated successfully."
fi
``` 