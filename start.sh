#!/bin/bash

# Start the backend and frontend applications

# Function to check if a command exists
command_exists() {
  command -v "$1" >/dev/null 2>&1
}

# Check if Maven is installed
if ! command_exists mvn; then
  echo "\033[0;31mMaven is not installed. Please install Maven to run the backend application.\033[0m"
  exit 1
fi

# Check if Node.js is installed
if ! command_exists node; then
  echo "\033[0;31mNode.js is not installed. Please install Node.js to run the frontend application.\033[0m"
  exit 1
fi

# Check if npm is installed
if ! command_exists npm; then
  echo "\033[0;31mnpm is not installed. Please install npm to run the frontend application.\033[0m"
  exit 1
fi

# Start the backend application in a new terminal window
echo "\033[0;32mStarting the backend application...\033[0m"
if command_exists gnome-terminal; then
  gnome-terminal -- bash -c "cd $(pwd) && mvn spring-boot:run; exec bash"
elif command_exists xterm; then
  xterm -e "cd $(pwd) && mvn spring-boot:run; exec bash" &
elif command_exists terminal; then
  terminal -e "cd $(pwd) && mvn spring-boot:run; exec bash" &
elif command_exists open; then
  # macOS
  open -a Terminal.app "$(pwd)/run-backend.sh"
  # Create a temporary script to run the backend
  echo "#!/bin/bash" > run-backend.sh
  echo "cd $(pwd) && mvn spring-boot:run" >> run-backend.sh
  chmod +x run-backend.sh
else
  echo "\033[0;33mCould not open a new terminal window. Starting backend in the background.\033[0m"
  cd $(pwd) && mvn spring-boot:run &
fi

# Navigate to the frontend directory
cd "$(pwd)/frontend"

# Check if node_modules exists, if not run npm install
if [ ! -d "node_modules" ]; then
  echo "\033[0;33mInstalling frontend dependencies...\033[0m"
  npm install
fi

# Start the frontend application
echo "\033[0;32mStarting the frontend application...\033[0m"
npm start