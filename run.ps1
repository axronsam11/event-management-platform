# PowerShell script to run the Event Management API locally

# Check if Java is installed
$javaVersion = java -version 2>&1 | Out-String
if (-not $javaVersion.Contains("version")) {
    Write-Host "Java is not installed or not in PATH. Please install Java 17 or higher." -ForegroundColor Red
    exit 1
}

# Check if Maven is installed
$mvnVersion = mvn --version 2>&1 | Out-String
if (-not $mvnVersion.Contains("Apache Maven")) {
    Write-Host "Maven is not installed or not in PATH. Please install Maven." -ForegroundColor Red
    exit 1
}

# Build the project
Write-Host "Building the project..." -ForegroundColor Cyan
mvn clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed. Please check the errors above." -ForegroundColor Red
    exit 1
}

# Run the application
Write-Host "Starting the application..." -ForegroundColor Green
java -jar target/event-management-api-0.0.1-SNAPSHOT.jar