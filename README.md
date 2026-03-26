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
  <a href="#documentation-work-in-progress">Documentation</a> &nbsp;&bull;&nbsp;
  <a href="#issues">Issues</a>
</p>



# Introduction

This project aims to demonstrate the **setup and management of a cloud-based cluster** using multiple instances on different clouds. It includes **NFS for file sharing**, **OpenMPI for distributed computing**, and automation scripts for infrastructure provisioning and configuration.

### Key features:

-  Automatically launch cluster nodes using any of the supported cloud providers.
-  Set up connectivity between nodes using SSH and NFS.
- ️ Automate software installation and environment setup.
-  Run distributed applications (e.g., ThermionsApp) with OpenMPI.

## Prerequisites
- Java 21 or superior.
## Installation

### 1. Clone the Repository
```bash
git clone https://github.com/bernardo-lopo/ClustEngine.git
cd ClustEngine
```
### 2. Configure the Project

Edit the configuration files in the config/ directory to match your desired setup.
Make sure to update values such as:
- AWS region and credentials
- Instance types
- Application-specific parameters

### 3. Build the Application
Use the Gradle wrapper to compile the project:
```bash
./gradlew build
```
### 4. Supported Cloud Providers
- AWS
- OpenStack

### 5. Run the Application
Execute the application using:
```bash
java -jar ConsoleAPP.jar
```
Ensure you have Java 21 or higher installed and accessible via your system’s PATH.

## Documentation (Work in Progress)

Full documentation is currently being prepared and will be released soon.

[Early PDF Draft](docs/ClustEngine_Documentation.pdf) – This is a preliminary version and may be incomplete or subject to changes.

> **Note:** Final documentation will include detailed guides, diagrams, and usage instructions.


## Issues

If you encounter any issues, bugs, or have suggestions for improvements, feel free to reach out through one of the following channels:

- [GitHub Issues](https://github.com/bernardo-lopo/ClustEngine/issues): Open a new issue to report a bug or request a feature.
- Discord: You can contact the maintainer directly via Discord at **bernas25**.

Your feedback is appreciated and helps improve the project !