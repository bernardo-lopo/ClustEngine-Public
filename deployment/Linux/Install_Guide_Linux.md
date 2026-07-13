# ClustEngine - Linux Installation Guide

Welcome to ClustEngine. This guide will walk you through the installation, configuration, and execution of the application on Linux or macOS environments.

## Prerequisites
Before starting the installation, ensure your system meets the following requirements:
* **Java Development Kit (JDK):** Version 21 or higher must be installed. You can verify your current version by running `java -version` in your terminal.
* **OpenSSH Client:** Must be installed (`ssh` command available in terminal). This is standard on macOS and most Linux distributions, but you can test it by running `ssh -V`.

## Installation Steps

### Option A: Production Mode
This mode installs the pre-built application, bypassing the need to compile the code on your machine.

1. **Download the Release:** Download the latest ClustEngine production release package (`.zip` or `.tar.gz`) and extract it to a folder of your choice.
2. **Navigate to the Folder:** Open your terminal application and move into the extracted directory.
   ```bash
   cd path/to/extracted/clustengine-release
   ```
3. **Move the .jar files:** : Move the compiled GuiAPP-Linux-1.0.0.jar file into the designated production directories. You must place a copy of the executable in the following location: deployment/Linux/prod/
4. **Grant Execution Permissions:** Give the production script permission to execute.
    ```bash
    chmod +x install_prod.sh
    ```
5. **Run the Installer:**
    ```bash
    ./install_prod.sh
    ```
### Option B: Dev Mode
1. **Clone the Repository:** Open your terminal application and download the project directly using Git.
   ```bash
   git clone https://github.com/bernardo-lopo/ClustEngine.git
   ```
2. **Navigate to the Deployment Folder:** Move into the cloned project directory and access the Linux/dev deployment folder.
3. **Grant Execution Permissions:** Before running the installer, you must give the script permission to execute. Run the following command:
   ```bash
   chmod +x install.sh
   ```
4. **Run the Installer:** To execute the script:
   ```bash
   ./install.sh
   ```
5. **Wait for Verification:** The script will automatically verify your file structure, confirm your Java version is adequate, and check for SSH availability. It will output its progress directly in the terminal so you can see exactly what is being configured.

## Where is the App Stored?

To keep your system clean and avoid requiring root or sudo privileges, ClustEngine is installed entirely within your user's home directory.

## Configuration

The installer generates a blank configuration file required to interface with your AWS and OpenStack infrastructure. You can configure your access details easily within the application itself:

1. Open ClustEngine using the desktop script (detailed below).
2. Navigate to the Settings screen in the application menu.
3. Fill in your specific cloud credentials, domain, region, and SSH key paths directly in the user interface.
4. Save your changes. The application will securely update the configuration files for you in the background.

## How to Run ClustEngine

The installation process automatically creates an execution script on your Desktop for easy access.

1. Go to your Desktop.
2. Run the run_clustengine.sh file.
