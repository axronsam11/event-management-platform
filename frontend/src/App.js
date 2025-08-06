import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';

// Layout components
import Layout from './components/layout/Layout';

// Auth components
import Login from './components/auth/Login';
import Register from './components/auth/Register';

// Event components
import HomePage from './components/home/HomePage';
import EventDetail from './components/events/EventDetail';
import EventForm from './components/events/EventForm';

// User components
import UserProfile from './components/profile/UserProfile';
import AdminDashboard from './components/admin/AdminDashboard';

// Context providers
import { AuthProvider } from './contexts/AuthContext';

// Route protection components
import PrivateRoute from './components/auth/PrivateRoute';
import RoleRoute from './components/auth/RoleRoute';

// Create a theme
const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
    background: {
      default: '#f5f5f5',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    h1: {
      fontSize: '2.5rem',
      fontWeight: 500,
    },
    h2: {
      fontSize: '2rem',
      fontWeight: 500,
    },
    h3: {
      fontSize: '1.75rem',
      fontWeight: 500,
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <Router>
          <Routes>
            {/* Public routes */}
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            
            {/* Routes within layout */}
            <Route path="/" element={<Layout />}>
              <Route index element={<HomePage />} />
              <Route path="events/:eventId" element={<EventDetail />} />
              
              {/* Protected routes */}
              <Route path="profile" element={
                <PrivateRoute>
                  <UserProfile />
                </PrivateRoute>
              } />
              
              {/* Organizer routes */}
              <Route path="events/create" element={
                <RoleRoute roles={['ORGANIZER', 'ADMIN']}>
                  <EventForm />
                </RoleRoute>
              } />
              <Route path="events/edit/:eventId" element={
                <RoleRoute roles={['ORGANIZER', 'ADMIN']}>
                  <EventForm />
                </RoleRoute>
              } />
              
              {/* Admin routes */}
              <Route path="admin" element={
                <RoleRoute roles={['ADMIN']}>
                  <AdminDashboard />
                </RoleRoute>
              } />
            </Route>
            
            {/* Fallback route */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;