# Event Management System - Frontend

## Overview

This is the frontend application for the Event Management System, built with React and Material-UI. It provides a user-friendly interface for managing events, user registrations, and administrative tasks.

## Features

- **User Authentication**: Register, login, and manage user profiles
- **Event Management**: Create, view, update, and delete events
- **Event Registration**: Register for events and view registered events
- **User Roles**: Different access levels for regular users, event organizers, and administrators
- **Notifications**: Real-time notifications for event updates and registrations
- **Admin Dashboard**: Manage users and events from a centralized dashboard
- **Responsive Design**: Works on desktop and mobile devices

## Prerequisites

- Node.js (v14 or higher)
- npm (v6 or higher)
- Backend API running (see backend documentation)

## Installation

1. Clone the repository
2. Navigate to the frontend directory:
   ```
   cd frontend
   ```
3. Install dependencies:
   ```
   npm install
   ```
4. Create a `.env` file in the root directory with the following content:
   ```
   REACT_APP_API_URL=http://localhost:8080/api
   ```
   (Adjust the URL if your backend is running on a different port)

## Running the Application

### Development Mode

```
npm start
```

This will start the development server on [http://localhost:3000](http://localhost:3000).

### Production Build

```
npm run build
```

This will create an optimized production build in the `build` folder.

## Project Structure

```
src/
├── components/        # UI components
│   ├── admin/         # Admin dashboard components
│   ├── auth/          # Authentication components
│   ├── events/        # Event-related components
│   ├── home/          # Home page components
│   ├── layout/        # Layout components
│   ├── notifications/ # Notification components
│   └── profile/       # User profile components
├── contexts/          # React contexts
│   └── AuthContext.js # Authentication context
├── services/          # API service functions
│   ├── eventService.js # Event-related API calls
│   └── userService.js  # User-related API calls
├── utils/             # Utility functions
│   └── dateUtils.js   # Date formatting utilities
├── App.js             # Main application component
└── index.js           # Application entry point
```

## Authentication

The application uses JWT (JSON Web Tokens) for authentication. The token is stored in local storage and included in the Authorization header for API requests.

## User Roles

- **USER**: Regular users who can view events and register for them
- **ORGANIZER**: Users who can create and manage their own events
- **ADMIN**: Users who have full access to the system, including user management

## API Integration

The frontend communicates with the backend API using Axios. API service functions are organized in the `services` directory.

## Styling

The application uses Material-UI for styling and components. Custom styles are defined in component files using the Material-UI styling system.

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature-name`
3. Commit your changes: `git commit -m 'Add some feature'`
4. Push to the branch: `git push origin feature-name`
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.