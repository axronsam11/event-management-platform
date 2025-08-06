# Event Management System

A comprehensive full-stack application for event management built with Spring Boot, MongoDB, and React. This system allows organizers to create and manage events, while attendees can browse events and register for them.

## Features

### User Management
- User registration and authentication
- Role-based access control (User, Organizer, Admin)
- User profile management
- In-app notifications system

### Event Management
- Create, read, update, and delete events
- Event categorization
- Event status tracking (Upcoming, Completed, Cancelled)
- Event registration with different ticket types
- Event search and filtering

### Admin Features
- User management (view users, update roles)
- Event oversight (view all events, update status, delete events)
- System monitoring

## Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.1.5**
- **Spring Security** with JWT authentication
- **MongoDB** for data storage
- **Spring Data MongoDB** for database operations
- **Lombok** for reducing boilerplate code
- **Validation** for input validation
- **OpenAPI/Swagger** for API documentation
- **Testcontainers** for integration testing

### Frontend
- **React** for building the user interface
- **Material-UI** for component styling
- **React Router** for navigation
- **Formik & Yup** for form handling and validation
- **Axios** for API communication
- **JWT-Decode** for token handling

## Project Structure

```
/
├── src/                  # Backend source code
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── eventmanagement/
│   │   │           └── api/
│   │   │               ├── controller/     # REST controllers
│   │   │               ├── dto/            # Data Transfer Objects
│   │   │               ├── exception/      # Custom exceptions and handler
│   │   │               ├── model/          # MongoDB document models
│   │   │               ├── repository/     # MongoDB repositories
│   │   │               ├── security/       # Security configuration and JWT
│   │   │               ├── service/        # Business logic
│   │   │               └── EventManagementApplication.java
│   │   └── resources/
│   │       └── application.yml             # Application configuration
│   └── test/
│       └── java/
│           └── com/
│               └── eventmanagement/
│                   └── api/
│                       └── controller/     # Integration tests
├── frontend/            # Frontend React application
│   ├── public/          # Public assets
│   ├── src/             # React source code
│   │   ├── components/  # UI components
│   │   ├── contexts/    # React contexts
│   │   ├── services/    # API service functions
│   │   └── utils/       # Utility functions
│   └── package.json     # Frontend dependencies
├── pom.xml              # Maven configuration
├── start.ps1            # Windows startup script
├── start.sh             # Unix startup script
└── README.md            # This file
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven
- Node.js 14 or higher
- npm 6 or higher
- MongoDB (local instance or MongoDB Atlas)

### Environment Setup

Create a `.env` file in the project root with the following variables:

```
MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/eventmanagement
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000
SERVER_PORT=8080
```

### Building and Running

#### Using the Startup Scripts

##### Windows

Run the PowerShell script:

```powershell
.\start.ps1
```

##### Unix/Linux/macOS

Run the bash script:

```bash
./start.sh
```

#### Manual Startup

##### Backend

```bash
# Build the project
mvn clean package

# Run the application
java -jar target/event-management-api-0.0.1-SNAPSHOT.jar
```

Alternatively, you can run it directly with Maven:

```bash
mvn spring-boot:run
```

##### Frontend

```bash
# Navigate to the frontend directory
cd frontend

# Install dependencies (first time only)
npm install

# Start the development server
npm start
```

The frontend will be available at http://localhost:3000

## Documentation

### API Documentation

Once the backend is running, you can access the Swagger UI at:

```
http://localhost:8080/api/swagger-ui.html
```

The OpenAPI specification is available at:

```
http://localhost:8080/api/v3/api-docs
```

### Frontend Documentation

The frontend includes the following main components:

- **Authentication**: Login and registration forms with validation
- **Layout**: Main application layout with navigation and notification center
- **Home Page**: Event listing with search and filtering capabilities
- **Event Management**: Detailed event view and event creation/editing forms
- **User Profile**: Profile management and notification handling
- **Admin Dashboard**: User and event management for administrators

## Authentication

### Backend Authentication

The API uses JWT for authentication. To access protected endpoints:

1. Register a user or login to get a JWT token
2. Include the token in the Authorization header of subsequent requests:
   ```
   Authorization: Bearer your_jwt_token
   ```

### Frontend Authentication

The frontend handles authentication through the `AuthContext` provider, which:

1. Manages login/registration through API calls
2. Stores JWT tokens in localStorage
3. Provides user information to components
4. Handles token expiration and refresh
5. Implements role-based access control for UI elements

## Testing

### Backend Testing

Run the backend tests with:

```bash
mvn test
```

Integration tests use Testcontainers to spin up a MongoDB instance, so Docker must be running on your machine.

### Frontend Testing

Run the frontend tests with:

```bash
cd frontend
npm test
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.