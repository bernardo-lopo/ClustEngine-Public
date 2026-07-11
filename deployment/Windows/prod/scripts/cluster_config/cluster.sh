#!/bin/bash

ROLE=$1
PROJECT_DIR="/home/ubuntu/projeto"
BUILD_LOG="/tmp/user_build.log"

echo "Setting up environment variables..."
export PATH=/home/ubuntu/projeto/mpi_inst/bin:$PATH
export LD_LIBRARY_PATH=/home/ubuntu/projeto/mpi_inst/lib:$LD_LIBRARY_PATH
sed -i "5i\export PATH=/home/ubuntu/projeto/mpi_inst/bin:\$PATH" ~/.bashrc
sed -i "5i\export LD_LIBRARY_PATH=/home/ubuntu/projeto/mpi_inst/lib:\$LD_LIBRARY_PATH" ~/.bashrc
source ~/.bashrc

if [ "$ROLE" == "primary" ]; then
    echo "User Script: Compiling MPI and Applications..."
    cd /tmp

    # Download and compile MPI
    wget https://download.open-mpi.org/release/open-mpi/v4.0/openmpi-4.0.1.tar.gz &>> $BUILD_LOG
    tar -xzvf openmpi-4.0.1.tar.gz &>> $BUILD_LOG
    cd openmpi-4.0.1
    ./configure --prefix=$PROJECT_DIR/mpi_inst &>> $BUILD_LOG
    make -j$(nproc) &>> $BUILD_LOG
    sudo make install &>> $BUILD_LOG
    sudo ldconfig

    # Download and compile Thermions
    cd $PROJECT_DIR
    wget --no-check-certificate "https://drive.google.com/uc?export=download&id=1RHEW2AMwrsRd1IolUkXyHd_gxrZpiXC7" -O ThermionsApp.zip
    #wget 'https://drive.usercontent.google.com/download?id=1pvI_-2ji0MDW1FGodZIOQfB1ia8-cceR&export=download&confirm=t' -O ThermionsApp-AMS.zip
    #unzip ThermionsApp-AMS.zip &>> $BUILD_LOG
    unzip ThermionsApp.zip &>> $BUILD_LOG

    cd ThermionsApp
    $PROJECT_DIR/mpi_inst/bin/mpicc MPIThermions/thermionSD.c -o MPIThermions/thermionSD -lm -O3 &>> $BUILD_LOG
    gcc SeqThermions/thermionSeq.c -o SeqThermions/thermionSeq -lm -O3 &>> $BUILD_LOG

elif [ "$ROLE" == "secondary" ]; then
    echo "User Script: Setting up secondary dependencies..."

    sudo DEBIAN_FRONTEND=noninteractive apt install -y libpmix-dev libhwloc-dev &>> $BUILD_LOG
    sudo ldconfig
fi

# Host file logic
if [ "$ROLE" == "primary" ]; then
    echo "User Script: Configuring MPI hosts..."
    HOSTS_FILE="$PROJECT_DIR/ThermionsApp/hosts"

    echo "User Script: Configuring MPI hosts..."

    mkdir -p "$(dirname "$HOSTS_FILE")"
    # Clears the file if it already exists
    > "$HOSTS_FILE"

    # Moves past the first two args (ROLE and original script path)
    # Then consume all remaining arguments which are the IPs
    shift 1

    # Shifts the list forward to completely delete the parsed flags
    for ip in "$@"; do
        echo "$ip slots=1" >> "$HOSTS_FILE"
    done

    echo "MPI hosts file successfully created at $HOSTS_FILE"
fi

echo "Checking MPI installation on $ROLE node..."
echo $PATH
mpirun --version
