# ClustEngine - Windows Installation Guide

Welcome to ClustEngine. This guide will walk you through the installation, configuration, and execution of the application on a Windows environment.

## Prerequisites
Before starting the installation, ensure your system meets the following requirements:
* **Java Development Kit (JDK):** Version 21 or higher must be installed and available in your system's `PATH`.
* **OpenSSH Client:** Must be installed (usually pre-installed on Windows 10/11, or available via *Windows Settings > Optional Features*).

## Installation Steps

### Option A: Production Mode
This mode installs the pre-built application, bypassing the need to compile the code on your machine.

1. **Download the Release:** Download the latest ClustEngine production release package (`.zip`) and extract it to a folder of your choice.
2. **Move the .jar files:** : Move the compiled GuiAPP-Windows-1.0.0.jar file into the designated production directories. You must place a copy of the executable in the following location: deployment/Windows/prod/

3. **Open PowerShell:** Press `Win + X` and select **Windows PowerShell** or **Terminal**.
4. **Navigate to the Folder:** Move into the extracted directory.
   ```powershell
   cd path\to\extracted\clustengine-release
    ```
5. **Run the Installer:** Execute the production script. (Note: If you receive a script execution disabled error, you may need to run Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass first).
    ```powershell
    .\install_prod.ps1
    ```
### Option B: Dev Mode
1. **Clone the Repository:** Open your terminal (Command Prompt or PowerShell) and download the project directly using Git.
   ```cmd
   git clone https://github.com/bernardo-lopo/ClustEngine.git
   ```
2. **Navigate to the Deployment Folder:** Move into the cloned project directory and access the Windows deployment folder.
   ```cmd
   cd ClustEngine\deployment\Windows
   ```
3. **Run the Installer:** You can run the installer by right-clicking the install.ps1 file in File Explorer and selecting "Run with PowerShell", or by executing it directly in your terminal:
    ```cmd
    .\install.ps1
    ```
4. **Wait for Verification:** The script will automatically verify your file structure, Java version, and SSH availability.

## Where is the App Stored?
To avoid requiring Administrator privileges, ClustEngine is installed entirely in the current user's local application data folder.
* **Installation Directory:** `%LOCALAPPDATA%\ClustEngine` *(You can paste this path directly into your File Explorer address bar).*

## Configuration
The installer generates a blank configuration file required to connect to AWS and OpenStack. You can configure your credentials easily within the application:
1. Open ClustEngine using the desktop shortcut.
2. Navigate to the **Settings** screen in the application menu.
3. Fill in your specific cloud credentials, domain, region, and SSH key paths directly in the user interface.
4. Save your changes. The application will securely update the configuration files for you.

## How to Run ClustEngine
The installation process automatically creates a convenient shortcut for you.
* Go to your **Desktop**.
* Double-click the **`run_clustengine.bat`** file.

This script will launch the application.