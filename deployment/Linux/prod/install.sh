#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
# No Color
NC='\033[0m'

echo "Starting ClustEngine Production installation..."

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &> /dev/null && pwd)"
cd "$SCRIPT_DIR" || { echo -e "${RED}Error: Could not navigate to deployment folder.${NC}"; exit 1; }

echo "Working directory set to release folder: $(pwd)"

# Verification for java version (>= 21)
echo "Checking Java version..."
if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed.${NC}"
    echo "Suggestion: Please install Java JDK 21 or higher."
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{if ($1 == 1) print $2; else print $1}')

if [ -z "$JAVA_VER" ] || [ "$JAVA_VER" -lt 21 ]; then
    echo -e "${RED}Error: Detected Java version is $JAVA_VER. Java 21 or higher is required.${NC}"
    echo "Suggestion: Please upgrade your Java installation to JDK 21 or higher."
    exit 1
fi

# Verification for folder structure
echo "Verifying installation files..."
FILES=(
    "GuiAPP-Linux-1.0.0.jar"
    "scripts"
    "config"
    "docs/Cluster Configuration.md"
    "config/aws_instances.json"
    "config/openstack_flavors.json"
    "scripts/cluster_config/base_setup.sh"
    "scripts/cluster_config/cluster.sh"
)

for file in "${FILES[@]}"; do
    if [ ! -e "$file" ]; then
        echo -e "${RED}Error: Missing required file or directory: '$file'${NC}"
        echo "Suggestion: Please ensure you have extracted all files from the production release package into this directory."
        exit 1
    fi
done

# Verification of the ssh client
echo "Checking for SSH client..."
if ! command -v ssh &> /dev/null; then
    echo -e "${RED}Error: SSH client not found.${NC}"
    echo "Suggestion: Please install OpenSSH (e.g., 'sudo apt install openssh-client' on Ubuntu, or enable it in macOS settings)."
    exit 1
fi

# Setup installation
INSTALL_DIR="$HOME/.clustengine"
echo "Installing ClustEngine to $INSTALL_DIR..."
mkdir -p "$INSTALL_DIR"
cp -r * "$INSTALL_DIR/"

# Creates the .env file
ENV_FILE="$INSTALL_DIR/.env"
echo "Generating .env file..."

cat > "$ENV_FILE" << EOF
BASE_SCRIPT_PATH="$INSTALL_DIR/scripts/cluster_config/base_setup.sh"
BASE_SCRIPT_NAME="base_setup.sh"
USER_SCRIPT_PATH="$INSTALL_DIR/scripts/cluster_config/cluster.sh"
USER_SCRIPT_NAME="cluster.sh"
OPENSTACK_USER_NAME=""
OPENSTACK_DOMAIN=""
OPENSTACK_PASSWORD=""
OPENSTACK_PROJECT_NAME=""
OPENSTACK_BASE_URL=""
OPENSTACK_KEY_FILE_PATH=""
OPENSTACK_IMAGE_ID=""
OPENSTACK_AVAILABILITY_ZONE=""
OPENSTACK_SECURITY_GROUP=""
OPENSTACK_NETWORK_ID=""
AWS_KEY_FILE_PATH=""
AWS_SUBNET_ID=""
AWS_SECURITY_GROUP_ID=""
AWS_IMAGE_ID=""
AWS_CLIENT_REGION=""
EOF

# Creates the run script shortcut on the desktop
DESKTOP_DIR="$HOME/Desktop"
if command -v xdg-user-dir &> /dev/null; then
    DESKTOP_DIR=$(xdg-user-dir DESKTOP)
fi

RUN_SCRIPT="$DESKTOP_DIR/run_clustengine.sh"
echo "Creating desktop shortcut at $RUN_SCRIPT..."

cat > "$RUN_SCRIPT" << EOF
#!/bin/bash
cd "$INSTALL_DIR"
java -Dkotlinx.coroutines.io.parallelism=512 -jar "$INSTALL_DIR/GuiAPP-Linux-1.0.0.jar"
EOF

chmod +x "$RUN_SCRIPT"

echo -e "${GREEN}Installation completed successfully!${NC}"
echo "You can now run ClustEngine using the 'run_clustengine.sh' script on your Desktop."