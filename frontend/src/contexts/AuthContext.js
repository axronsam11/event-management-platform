import React, { createContext, useState, useEffect, useContext } from 'react';
import axios from 'axios';
import jwtDecode from 'jwt-decode';

const AuthContext = createContext();

export function useAuth() {
  return useContext(AuthContext);
}

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    // Check if token exists and is valid
    if (token) {
      try {
        // Decode token to get user info
        const decoded = jwtDecode(token);
        
        // Check if token is expired
        const currentTime = Date.now() / 1000;
        if (decoded.exp < currentTime) {
          // Token is expired
          logout();
          return;
        }
        
        // Set axios default header
        axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
        
        // Get user profile
        axios.get('/api/users/me')
          .then(response => {
            setCurrentUser(response.data);
            setLoading(false);
          })
          .catch(err => {
            console.error('Error fetching user profile:', err);
            logout();
          });
      } catch (err) {
        console.error('Invalid token:', err);
        logout();
      }
    } else {
      setLoading(false);
    }
  }, [token]);

  // Login function
  const login = async (email, password) => {
    try {
      setError('');
      const response = await axios.post('/api/auth/login', { email, password });
      const { token: newToken, ...userData } = response.data;
      
      // Save token to localStorage
      localStorage.setItem('token', newToken);
      
      // Set token in state and axios headers
      setToken(newToken);
      axios.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
      
      // Set user data
      setCurrentUser(userData);
      
      return userData;
    } catch (err) {
      console.error('Login error:', err);
      setError(err.response?.data?.message || 'Failed to login');
      throw err;
    }
  };

  // Register function
  const register = async (userData) => {
    try {
      setError('');
      const response = await axios.post('/api/auth/register', userData);
      const { token: newToken, ...user } = response.data;
      
      // Save token to localStorage
      localStorage.setItem('token', newToken);
      
      // Set token in state and axios headers
      setToken(newToken);
      axios.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
      
      // Set user data
      setCurrentUser(user);
      
      return user;
    } catch (err) {
      console.error('Registration error:', err);
      setError(err.response?.data?.message || 'Failed to register');
      throw err;
    }
  };

  // Logout function
  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setCurrentUser(null);
    delete axios.defaults.headers.common['Authorization'];
  };

  // Check if user has a specific role
  const hasRole = (role) => {
    if (!currentUser || !currentUser.roles) return false;
    return currentUser.roles.includes(role);
  };

  // Update user profile
  const updateProfile = async (profileData) => {
    try {
      const response = await axios.put('/api/users/me', profileData);
      setCurrentUser(prev => ({ ...prev, ...response.data }));
      return response.data;
    } catch (err) {
      console.error('Profile update error:', err);
      setError(err.response?.data?.message || 'Failed to update profile');
      throw err;
    }
  };

  const value = {
    currentUser,
    token,
    loading,
    error,
    login,
    register,
    logout,
    hasRole,
    updateProfile,
    isAuthenticated: !!currentUser,
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
}