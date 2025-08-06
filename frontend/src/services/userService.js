import axios from 'axios';

const API_URL = '/api/users';

// Get current user profile
export const getCurrentUserProfile = async () => {
  try {
    const response = await axios.get(`${API_URL}/me`);
    return response.data;
  } catch (error) {
    console.error('Error fetching user profile:', error);
    throw error;
  }
};

// Update current user profile
export const updateUserProfile = async (profileData) => {
  try {
    const response = await axios.put(`${API_URL}/me`, profileData);
    return response.data;
  } catch (error) {
    console.error('Error updating user profile:', error);
    throw error;
  }
};

// Get user notifications
export const getUserNotifications = async () => {
  try {
    const response = await axios.get(`${API_URL}/me/notifications`);
    return response.data;
  } catch (error) {
    console.error('Error fetching user notifications:', error);
    throw error;
  }
};

// Mark notification as read
export const markNotificationAsRead = async (notificationId) => {
  try {
    const response = await axios.put(`${API_URL}/me/notifications/${notificationId}/read`);
    return response.data;
  } catch (error) {
    console.error(`Error marking notification ${notificationId} as read:`, error);
    throw error;
  }
};

// Admin: Get all users
export const getAllUsers = async (page = 0, size = 10) => {
  try {
    const response = await axios.get('/api/admin/users', { params: { page, size } });
    return response.data;
  } catch (error) {
    console.error('Error fetching all users:', error);
    throw error;
  }
};

// Admin: Get user by ID
export const getUserById = async (userId) => {
  try {
    const response = await axios.get(`${API_URL}/${userId}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching user ${userId}:`, error);
    throw error;
  }
};

// Admin: Update user roles
export const updateUserRoles = async (userId, roles) => {
  try {
    const response = await axios.put(`/api/admin/users/${userId}/roles`, { roles });
    return response.data;
  } catch (error) {
    console.error(`Error updating roles for user ${userId}:`, error);
    throw error;
  }
};

// Admin: Disable/Enable user account
export const toggleUserStatus = async (userId, enabled) => {
  try {
    const response = await axios.put(`/api/admin/users/${userId}/status`, { enabled });
    return response.data;
  } catch (error) {
    console.error(`Error toggling status for user ${userId}:`, error);
    throw error;
  }
};