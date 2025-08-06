import React, { useState, useEffect } from 'react';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import {
  Container,
  Grid,
  Paper,
  Typography,
  Box,
  TextField,
  Button,
  Tabs,
  Tab,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Divider,
  Card,
  CardContent,
  CardActions,
  Alert,
  CircularProgress,
  Badge,
  Chip
} from '@mui/material';
import {
  Edit as EditIcon,
  Save as SaveIcon,
  Cancel as CancelIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
  MarkEmailRead as MarkReadIcon
} from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';
import { getCurrentUserProfile, updateUserProfile, getUserNotifications, markNotificationAsRead } from '../../services/userService';
import { getEventsOrganizedByUser, getEventsRegisteredByUser } from '../../services/eventService';
import { formatDate } from '../../utils/dateUtils';

const UserProfile = () => {
  const { currentUser, updateCurrentUser } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [tabValue, setTabValue] = useState(0);
  const [editMode, setEditMode] = useState(false);
  const [saving, setSaving] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [organizedEvents, setOrganizedEvents] = useState([]);
  const [registeredEvents, setRegisteredEvents] = useState([]);
  const [loadingEvents, setLoadingEvents] = useState(false);

  useEffect(() => {
    fetchUserProfile();
    fetchNotifications();
  }, []);

  const fetchUserProfile = async () => {
    try {
      setLoading(true);
      const data = await getCurrentUserProfile();
      setProfile(data);
    } catch (err) {
      console.error('Error fetching user profile:', err);
      setError('Failed to load user profile. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const fetchNotifications = async () => {
    try {
      const data = await getUserNotifications();
      setNotifications(data);
    } catch (err) {
      console.error('Error fetching notifications:', err);
    }
  };

  const fetchUserEvents = async () => {
    try {
      setLoadingEvents(true);
      const [organized, registered] = await Promise.all([
        getEventsOrganizedByUser(),
        getEventsRegisteredByUser()
      ]);
      setOrganizedEvents(organized);
      setRegisteredEvents(registered);
    } catch (err) {
      console.error('Error fetching user events:', err);
    } finally {
      setLoadingEvents(false);
    }
  };

  useEffect(() => {
    if (tabValue === 1) {
      fetchUserEvents();
    }
  }, [tabValue]);

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  const handleMarkAsRead = async (notificationId) => {
    try {
      await markNotificationAsRead(notificationId);
      setNotifications(notifications.map(notification => 
        notification.id === notificationId 
          ? { ...notification, read: true } 
          : notification
      ));
    } catch (err) {
      console.error('Error marking notification as read:', err);
    }
  };

  const validationSchema = Yup.object({
    firstName: Yup.string().required('First name is required'),
    lastName: Yup.string().required('Last name is required'),
    email: Yup.string().email('Invalid email address').required('Email is required'),
    phoneNumber: Yup.string().matches(/^[0-9+\-\s]+$/, 'Invalid phone number').required('Phone number is required')
  });

  const formik = useFormik({
    initialValues: {
      firstName: profile?.firstName || '',
      lastName: profile?.lastName || '',
      email: profile?.email || '',
      phoneNumber: profile?.phoneNumber || ''
    },
    validationSchema,
    enableReinitialize: true,
    onSubmit: async (values) => {
      try {
        setSaving(true);
        setError('');
        const updatedProfile = await updateUserProfile(values);
        setProfile(updatedProfile);
        updateCurrentUser({
          ...currentUser,
          firstName: updatedProfile.firstName,
          lastName: updatedProfile.lastName
        });
        setEditMode(false);
      } catch (err) {
        console.error('Error updating profile:', err);
        setError(err.response?.data?.message || 'Failed to update profile. Please try again.');
      } finally {
        setSaving(false);
      }
    }
  });

  const handleEditToggle = () => {
    if (editMode) {
      formik.resetForm();
    }
    setEditMode(!editMode);
  };

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
          <CircularProgress />
        </Box>
      </Container>
    );
  }

  if (error) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="error" sx={{ mb: 4 }}>
          {error}
        </Alert>
      </Container>
    );
  }

  const unreadNotificationsCount = notifications.filter(notification => !notification.read).length;

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Grid container spacing={4}>
        <Grid item xs={12} md={4}>
          <Paper elevation={3} sx={{ p: 3, mb: 3 }}>
            <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 3 }}>
              <Badge
                overlap="circular"
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
                badgeContent={
                  editMode ? (
                    <IconButton 
                      size="small" 
                      sx={{ bgcolor: 'primary.main', color: 'white', '&:hover': { bgcolor: 'primary.dark' } }}
                      onClick={handleEditToggle}
                    >
                      <CancelIcon fontSize="small" />
                    </IconButton>
                  ) : (
                    <IconButton 
                      size="small" 
                      sx={{ bgcolor: 'primary.main', color: 'white', '&:hover': { bgcolor: 'primary.dark' } }}
                      onClick={handleEditToggle}
                    >
                      <EditIcon fontSize="small" />
                    </IconButton>
                  )
                }
              >
                <Box
                  sx={{
                    width: 100,
                    height: 100,
                    borderRadius: '50%',
                    bgcolor: 'primary.main',
                    color: 'white',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '2rem',
                    fontWeight: 'bold',
                    mb: 2
                  }}
                >
                  {profile?.firstName?.charAt(0)}{profile?.lastName?.charAt(0)}
                </Box>
              </Badge>
              <Typography variant="h5" gutterBottom>
                {profile?.firstName} {profile?.lastName}
              </Typography>
              <Box sx={{ display: 'flex', gap: 1 }}>
                {profile?.roles?.map((role, index) => (
                  <Chip 
                    key={index} 
                    label={role} 
                    color={role === 'ADMIN' ? 'error' : role === 'ORGANIZER' ? 'primary' : 'default'}
                    size="small"
                  />
                ))}
              </Box>
            </Box>

            {editMode ? (
              <Box component="form" onSubmit={formik.handleSubmit}>
                <Grid container spacing={2}>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      id="firstName"
                      name="firstName"
                      label="First Name"
                      value={formik.values.firstName}
                      onChange={formik.handleChange}
                      onBlur={formik.handleBlur}
                      error={formik.touched.firstName && Boolean(formik.errors.firstName)}
                      helperText={formik.touched.firstName && formik.errors.firstName}
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      id="lastName"
                      name="lastName"
                      label="Last Name"
                      value={formik.values.lastName}
                      onChange={formik.handleChange}
                      onBlur={formik.handleBlur}
                      error={formik.touched.lastName && Boolean(formik.errors.lastName)}
                      helperText={formik.touched.lastName && formik.errors.lastName}
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      id="email"
                      name="email"
                      label="Email"
                      value={formik.values.email}
                      onChange={formik.handleChange}
                      onBlur={formik.handleBlur}
                      error={formik.touched.email && Boolean(formik.errors.email)}
                      helperText={formik.touched.email && formik.errors.email}
                      disabled
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <TextField
                      fullWidth
                      id="phoneNumber"
                      name="phoneNumber"
                      label="Phone Number"
                      value={formik.values.phoneNumber}
                      onChange={formik.handleChange}
                      onBlur={formik.handleBlur}
                      error={formik.touched.phoneNumber && Boolean(formik.errors.phoneNumber)}
                      helperText={formik.touched.phoneNumber && formik.errors.phoneNumber}
                    />
                  </Grid>
                  <Grid item xs={12}>
                    <Button
                      type="submit"
                      variant="contained"
                      startIcon={<SaveIcon />}
                      disabled={saving}
                      fullWidth
                    >
                      {saving ? <CircularProgress size={24} /> : 'Save Changes'}
                    </Button>
                  </Grid>
                </Grid>
              </Box>
            ) : (
              <List>
                <ListItem>
                  <ListItemText primary="Email" secondary={profile?.email} />
                </ListItem>
                <Divider component="li" />
                <ListItem>
                  <ListItemText primary="Phone Number" secondary={profile?.phoneNumber} />
                </ListItem>
                <Divider component="li" />
                <ListItem>
                  <ListItemText 
                    primary="Member Since" 
                    secondary={formatDate(profile?.createdAt)} 
                  />
                </ListItem>
              </List>
            )}
          </Paper>
        </Grid>

        <Grid item xs={12} md={8}>
          <Paper elevation={3} sx={{ p: 3 }}>
            <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
              <Tabs value={tabValue} onChange={handleTabChange} aria-label="profile tabs">
                <Tab 
                  label={
                    <Badge 
                      color="error" 
                      badgeContent={unreadNotificationsCount} 
                      max={99}
                      showZero={false}
                    >
                      Notifications
                    </Badge>
                  } 
                  id="tab-0" 
                />
                <Tab label="My Events" id="tab-1" />
              </Tabs>
            </Box>

            <Box role="tabpanel" hidden={tabValue !== 0}>
              {tabValue === 0 && (
                <>
                  {notifications.length === 0 ? (
                    <Typography variant="body1" color="text.secondary" sx={{ py: 2, textAlign: 'center' }}>
                      You have no notifications.
                    </Typography>
                  ) : (
                    <List>
                      {notifications.map((notification) => (
                        <React.Fragment key={notification.id}>
                          <ListItem 
                            alignItems="flex-start"
                            sx={{
                              bgcolor: notification.read ? 'transparent' : 'rgba(0, 0, 0, 0.04)',
                              borderRadius: 1
                            }}
                          >
                            <ListItemText
                              primary={
                                <Typography 
                                  variant="subtitle1" 
                                  component="span"
                                  sx={{ fontWeight: notification.read ? 'normal' : 'bold' }}
                                >
                                  {notification.title}
                                </Typography>
                              }
                              secondary={
                                <>
                                  <Typography variant="body2" component="span" display="block">
                                    {notification.message}
                                  </Typography>
                                  <Typography variant="caption" color="text.secondary">
                                    {formatDate(notification.createdAt)}
                                  </Typography>
                                </>
                              }
                            />
                            <ListItemSecondaryAction>
                              {!notification.read && (
                                <IconButton 
                                  edge="end" 
                                  aria-label="mark as read"
                                  onClick={() => handleMarkAsRead(notification.id)}
                                >
                                  <MarkReadIcon />
                                </IconButton>
                              )}
                            </ListItemSecondaryAction>
                          </ListItem>
                          <Divider component="li" variant="inset" />
                        </React.Fragment>
                      ))}
                    </List>
                  )}
                </>
              )}
            </Box>

            <Box role="tabpanel" hidden={tabValue !== 1}>
              {tabValue === 1 && (
                <>
                  {loadingEvents ? (
                    <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
                      <CircularProgress />
                    </Box>
                  ) : (
                    <>
                      <Typography variant="h6" gutterBottom>
                        Events I'm Organizing
                      </Typography>
                      {organizedEvents.length === 0 ? (
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 4 }}>
                          You haven't organized any events yet.
                        </Typography>
                      ) : (
                        <Grid container spacing={2} sx={{ mb: 4 }}>
                          {organizedEvents.map((event) => (
                            <Grid item xs={12} sm={6} key={event.id}>
                              <Card variant="outlined">
                                <CardContent>
                                  <Typography variant="h6" gutterBottom>
                                    {event.title}
                                  </Typography>
                                  <Box sx={{ display: 'flex', gap: 1, mb: 1 }}>
                                    <Chip 
                                      label={event.category.charAt(0) + event.category.slice(1).toLowerCase()} 
                                      size="small" 
                                      color="primary" 
                                      variant="outlined"
                                    />
                                    <Chip 
                                      label={event.status} 
                                      size="small" 
                                      color={event.status === 'UPCOMING' ? 'success' : 'default'}
                                      variant="outlined"
                                    />
                                  </Box>
                                  <Typography variant="body2" color="text.secondary">
                                    {formatDate(event.startDate)}
                                  </Typography>
                                  <Typography variant="body2" color="text.secondary">
                                    {event.location}
                                  </Typography>
                                </CardContent>
                                <CardActions>
                                  <Button 
                                    size="small" 
                                    startIcon={<ViewIcon />}
                                    href={`/events/${event.id}`}
                                  >
                                    View
                                  </Button>
                                  <Button 
                                    size="small" 
                                    startIcon={<EditIcon />}
                                    href={`/events/${event.id}/edit`}
                                  >
                                    Edit
                                  </Button>
                                </CardActions>
                              </Card>
                            </Grid>
                          ))}
                        </Grid>
                      )}

                      <Typography variant="h6" gutterBottom>
                        Events I'm Attending
                      </Typography>
                      {registeredEvents.length === 0 ? (
                        <Typography variant="body2" color="text.secondary">
                          You haven't registered for any events yet.
                        </Typography>
                      ) : (
                        <Grid container spacing={2}>
                          {registeredEvents.map((event) => (
                            <Grid item xs={12} sm={6} key={event.id}>
                              <Card variant="outlined">
                                <CardContent>
                                  <Typography variant="h6" gutterBottom>
                                    {event.title}
                                  </Typography>
                                  <Box sx={{ display: 'flex', gap: 1, mb: 1 }}>
                                    <Chip 
                                      label={event.category.charAt(0) + event.category.slice(1).toLowerCase()} 
                                      size="small" 
                                      color="primary" 
                                      variant="outlined"
                                    />
                                    <Chip 
                                      label={event.status} 
                                      size="small" 
                                      color={event.status === 'UPCOMING' ? 'success' : 'default'}
                                      variant="outlined"
                                    />
                                  </Box>
                                  <Typography variant="body2" color="text.secondary">
                                    {formatDate(event.startDate)}
                                  </Typography>
                                  <Typography variant="body2" color="text.secondary">
                                    {event.location}
                                  </Typography>
                                </CardContent>
                                <CardActions>
                                  <Button 
                                    size="small" 
                                    startIcon={<ViewIcon />}
                                    href={`/events/${event.id}`}
                                  >
                                    View
                                  </Button>
                                </CardActions>
                              </Card>
                            </Grid>
                          ))}
                        </Grid>
                      )}
                    </>
                  )}
                </>
              )}
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default UserProfile;