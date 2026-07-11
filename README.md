<h1 align="center">
  <a href="https://github.com/bernardo-lopo/Cluster">
    <img src="https://i.imgur.com/DZh3azP.png" alt="ClustEngine" height="307" width="460">
  </a>
</h1>

<p align="center">
  This repository contains the source code for the project <strong>ClustEngine</strong>.<br/>
  A complete solution to deploy, configure, and automate clusters in the cloud using modern infrastructure tools and automation scripts.
</p>

<p align="center">
  <a href="#introduction">Introduction</a> &nbsp;&bull;&nbsp;
  <a href="#prerequisites">Prerequisites</a> &nbsp;&bull;&nbsp;
  <a href="#installation">Installation</a> &nbsp;&bull;&nbsp;
  <a href="#gui-application-and-settings">GUI Application and Settings</a> &nbsp;&bull;&nbsp;
  <a href="#documentation">Documentation</a> &nbsp;&bull;&nbsp;
  <a href="#issues">Issues</a>
</p>

## Introduction

This project aims to demonstrate the setup and management of a cloud-based cluster using multiple instances on different clouds. It includes NFS for file sharing, OpenMPI for distributed computing, and automation scripts for infrastructure provisioning and configuration.

### Key features:

- Automatically launch cluster nodes using any of the supported cloud providers (AWS and OpenStack).
- Set up connectivity between nodes using SSH and NFS.
- Automate software installation and environment setup.
- Run distributed applications (e.g., ThermionsApp) with OpenMPI.

## Prerequisites

- Java 21 or superior.
- OpenSSH Client.
- Git (Optional)

## Installation

The installation process is automated through dedicated deployment scripts. Please refer to the specific guide for your operating system located in the `deployment` folder:

### Windows
1. Navigate to the `deployment/Windows/` directory.
2. Read the [Install_Guide_Windows.md](./deployment/Windows/Install_Guide_Windows.md) file for full instructions.
3. Run the `install.ps1` (or provided batch wrapper) script to verify dependencies and set up the application environment automatically.

### Linux
1. Navigate to the `deployment/Linux` directory.
2. Read the [Install_Guide_Linux_Mac.md](deployment/Linux/Install_Guide_Linux.md) file for full instructions.
3. Grant execution permissions and run the `install.sh` script to verify dependencies, configure paths, and deploy the application to your home directory.

## GUI Application and Settings

ClustEngine features a complete Desktop Graphical User Interface (GUIApp) to simplify cluster management, and monitoring.

### Launching the Application
Once installed using the deployment scripts, a shortcut will be generated on your desktop. Use this shortcut to launch the GUIApp.

### Settings Management
Instead of manually editing configuration files in the `config/` directory, you can now manage your entire environment directly from the GUI Settings screen:
- **General Tab:** Customize your user experience by selecting your preferred application theme (Light, Dark, or Auto) and language.
- **Script Injection Tab:** Easily select and define the absolute paths for your core automation scripts (`base_setup.sh` and `cluster.sh`) using the built-in file picker.
- **Credentials Tab:** Securely input your cloud provider details. This includes AWS configurations (Region, Subnet ID, Security Group ID, AMI) and OpenStack configurations (Credentials, Domain, Base URL, Image ID, Network ID).

All configurations are securely saved locally to your environment directory and loaded automatically on subsequent runs.

## Documentation

The project documentation is included directly in this repository. You can use the shortcuts below to navigate the documentation files:

- **[Browse Documentation Directory](./docs/)**: View all available documentation and resources.

## Issues

If you encounter any issues, bugs, or have suggestions for improvements, feel free to reach out through one of the following channels:

- [GitHub Issues](https://github.com/bernardo-lopo/ClustEngine/issues): Open a new issue to report a bug or request a feature.
- Discord: You can contact the maintainer directly via Discord at **bernas25**.

Your feedback is appreciated and helps improve the project!