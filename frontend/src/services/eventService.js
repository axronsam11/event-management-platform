import axios from 'axios';

const API_URL = '/api/events';

// Get all events with optional pagination and filtering
export const getEvents = async (page = 0, size = 10, filters = {}) => {
  try {
    const params = { page, size, ...filters };
    const response = await axios.get(API_URL, { params });
    return response.data;
  } catch (error) {
    console.error('Error fetching events:', error);
    throw error;
  }
};

// Get a single event by ID
export const getEventById = async (eventId) => {
  try {
    const response = await axios.get(`${API_URL}/${eventId}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching event ${eventId}:`, error);
    throw error;
  }
};

// Create a new event
export const createEvent = async (eventData) => {
  try {
    const response = await axios.post(API_URL, eventData);
    return response.data;
  } catch (error) {
    console.error('Error creating event:', error);
    throw error;
  }
};

// Update an existing event
export const updateEvent = async (eventId, eventData) => {
  try {
    const response = await axios.put(`${API_URL}/${eventId}`, eventData);
    return response.data;
  } catch (error) {
    console.error(`Error updating event ${eventId}:`, error);
    throw error;
  }
};

// Delete an event
export const deleteEvent = async (eventId) => {
  try {
    const response = await axios.delete(`${API_URL}/${eventId}`);
    return response.data;
  } catch (error) {
    console.error(`Error deleting event ${eventId}:`, error);
    throw error;
  }
};

// Register for an event
export const registerForEvent = async (eventId, registrationData) => {
  try {
    const response = await axios.post(`${API_URL}/${eventId}/register`, registrationData);
    return response.data;
  } catch (error) {
    console.error(`Error registering for event ${eventId}:`, error);
    throw error;
  }
};

// Get event registrations (for organizers)
export const getEventRegistrations = async (eventId) => {
  try {
    const response = await axios.get(`${API_URL}/${eventId}/registrations`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching registrations for event ${eventId}:`, error);
    throw error;
  }
};

// Get events organized by current user
export const getMyEvents = async () => {
  try {
    const response = await axios.get(`${API_URL}/organizer/me`);
    return response.data;
  } catch (error) {
    console.error('Error fetching my events:', error);
    throw error;
  }
};

// Get events user is registered for
export const getMyRegisteredEvents = async () => {
  try {
    const response = await axios.get('/api/users/me/registrations');
    return response.data;
  } catch (error) {
    console.error('Error fetching registered events:', error);
    throw error;
  }
};