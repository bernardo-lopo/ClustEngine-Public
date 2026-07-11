Write-Host "Starting ClustEngine Production installation..." -ForegroundColor Cyan

# Navigate to the directory where this script is located.
$projectRoot = $PSScriptRoot
Set-Location -Path $projectRoot

Write-Host "Working directory set to release folder: $((Get-Location).Path)"

# Verification for java version (>= 21)
Write-Host "Checking Java version..."
if (-Not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host "Error: Java is not installed or not in the system PATH." -ForegroundColor Red
    Write-Host "Suggestion: Please install Java JDK 21 or higher and restart your terminal."
    exit 1
}

$javaVersionOutput = java -version 2>&1
$versionMatch = $javaVersionOutput | Select-String -Pattern 'version "([^"]+)"'
if ($versionMatch) {
    $versionString = $versionMatch.Matches.Groups[1].Value
    $majorVersion = $versionString.Split('.')[0]
    if ($majorVersion -eq "1") {
        $majorVersion = $versionString.Split('.')[1]
    }

    if ([int]$majorVersion -lt 21) {
        Write-Host "Error: Detected Java version is $majorVersion. Java 21 or higher is required." -ForegroundColor Red
        Write-Host "Suggestion: Please upgrade your Java installation to JDK 21 or higher."
        exit 1
    }
} else {
    Write-Host "Warning: Could not parse Java version, proceeding anyway..." -ForegroundColor Yellow
}

# Verification for the folder structure
Write-Host "Verifying installation files..."
$files = @(
    "GuiAPP-Windows-1.0.0.jar",
    "scripts",
    "config",
    "Cluster Configuration.md",
    "config\aws_instances.json",
    "config\openstack_flavors.json",
    "scripts\cluster_config\base_setup.sh",
    "scripts\cluster_config\cluster.sh"
)

foreach ($file in $files) {
    if (-Not (Test-Path -Path $file)) {
        Write-Host "Error: Missing required file or directory: '$file'" -ForegroundColor Red
        Write-Host "Suggestion: Please ensure you have extracted all files from the production release package into this directory."
        exit 1
    }
}

# Verification of the ssh client
Write-Host "Checking for SSH client..."
if (-Not (Get-Command ssh -ErrorAction SilentlyContinue)) {
    Write-Host "Error: SSH client not found." -ForegroundColor Red
    Write-Host "Suggestion: Please install OpenSSH Client via Windows Settings > Optional Features."
    exit 1
}

# Setup Installation
$installDir = "$env:LOCALAPPDATA\ClustEngine"
Write-Host "Installing ClustEngine to $installDir..."
if (Test-Path $installDir) {
    Remove-Item -Recurse -Force "$installDir\*"
} else {
    New-Item -ItemType Directory -Force -Path $installDir | Out-Null
}
Copy-Item -Path "*" -Destination $installDir -Recurse -Force

# Creates the .env file
$envFile = Join-Path $installDir ".env"
Write-Host "Generating .env file..."

$baseScriptPath = (Join-Path $installDir "scripts\cluster_config\base_setup.sh").Replace("\", "/")
$userScriptPath = (Join-Path $installDir "scripts\cluster_config\cluster.sh").Replace("\", "/")

$envContent = @"
BASE_SCRIPT_PATH="$baseScriptPath"
BASE_SCRIPT_NAME="base_setup.sh"
USER_SCRIPT_PATH="$userScriptPath"
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
"@
Set-Content -Path $envFile -Value $envContent -Encoding UTF8

# Creates the run script shortcut on desktop
$desktopDir = [Environment]::GetFolderPath("Desktop")
$runScript = Join-Path $desktopDir "run_clustengine.bat"
Write-Host "Creating desktop shortcut at $runScript..."

$batContent = @"
@echo off
cd /d "$installDir"
java -Dkotlinx.coroutines.io.parallelism=512 -jar "$installDir\GuiAPP-Windows-1.0.0.jar"
"@
Set-Content -Path $runScript -Value $batContent -Encoding Default

Write-Host "`nInstallation completed successfully!" -ForegroundColor Green
Write-Host "You can now run ClustEngine using the 'run_clustengine.bat' shortcut on your Desktop."
Write-Host "Press any key to exit..."
$Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown") | Out-Null