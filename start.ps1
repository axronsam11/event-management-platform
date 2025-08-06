# Start the backend and frontend applications

# Function to check if a command exists
function Test-CommandExists {
    param ($command)
    $exists = $null -ne (Get-Command $command -ErrorAction SilentlyContinue)
    return $exists
}

# Check if Maven is installed
if (-not (Test-CommandExists "mvn")) {
    Write-Host "Maven is not installed. Please install Maven to run the backend application." -ForegroundColor Red
    exit 1
}

# Check if Node.js is installed
if (-not (Test-CommandExists "node")) {
    Write-Host "Node.js is not installed. Please install Node.js to run the frontend application." -ForegroundColor Red
    exit 1
}

# Check if npm is installed
if (-not (Test-CommandExists "npm")) {
    Write-Host "npm is not installed. Please install npm to run the frontend application." -ForegroundColor Red
    exit 1
}

# Start the backend application in a new PowerShell window
Write-Host "Starting the backend application..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd $PSScriptRoot; mvn spring-boot:run"

# Navigate to the frontend directory
Set-Location -Path "$PSScriptRoot\frontend"

# Check if node_modules exists, if not run npm install
if (-not (Test-Path -Path "node_modules")) {
    Write-Host "Installing frontend dependencies..." -ForegroundColor Yellow
    npm install
}

# Start the frontend application
Write-Host "Starting the frontend application..." -ForegroundColor Green
npm start