#!/bin/bash

# List of IPs
# To future use i will change to receive the instance ips has a parameter
IPS=(
192.168.1.189
192.168.1.17
192.168.1.105
192.168.1.229
)

# Path to the source file (on the current machine)
SOURCE_BASHRC="$HOME/.bashrc"

for ip in "${IPS[@]}"; do
    echo "Updating .bashrc on $ip..."
    scp -o StrictHostKeyChecking=no "$SOURCE_BASHRC" ubuntu@"$ip":.bashrc
done

echo "Update completed on all machines."
