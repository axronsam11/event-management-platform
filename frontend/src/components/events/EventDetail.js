import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Grid,
  Typography,
  Box,
  Button,
  Card,
  CardContent,
  CardMedia,
  Chip,
  Divider,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Avatar,
  Paper,
  Tab,
  Tabs,
  CircularProgress,
  Alert,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle
} from '@mui/material';
import {
  LocationOn,
  CalendarToday,
  Person,
  Info,
  Schedule,
  ConfirmationNumber,
  Edit,
  Delete
} from '@mui/icons-material';
import { getEventById, registerForEvent, deleteEvent } from '../../services/eventService';
import { useAuth } from '../../contexts/AuthContext';
import { formatDateRange, formatDate } from '../../utils/dateUtils';

const EventDetail = () => {
  const { eventId } = useParams();
  const navigate = useNavigate();
  const { currentUser, isAuthenticated, hasRole } = useAuth();
  
  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [tabValue, setTabValue] = useState(0);
  const [registering, setRegistering] = useState(false);
  const [registerError, setRegisterError] = useState('');
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    fetchEventDetails();
  }, [eventId]);

  const fetchEventDetails = async () => {
    try {
      setLoading(true);
      const data = await getEventById(eventId);
      setEvent(data);
    } catch (err) {
      console.error('Error fetching event details:', err);
      setError('Failed to load event details. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  const handleRegisterForEvent = async () => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: `/events/${eventId}` } });
      return;
    }

    try {
      setRegistering(true);
      setRegisterError('');
      await registerForEvent(eventId);
      // Refresh event details to update registration status
      await fetchEventDetails();
    } catch (err) {
      console.error('Error registering for event:', err);
      setRegisterError(err.response?.data?.message || 'Failed to register for this event. Please try again.');
    } finally {
      setRegistering(false);
    }
  };

  const handleEditEvent = () => {
    navigate(`/events/${eventId}/edit`);
  };

  const handleDeleteEvent = async () => {
    try {
      setDeleting(true);
      await deleteEvent(eventId);
      navigate('/');
    } catch (err) {
      console.error('Error deleting event:', err);
      setError(err.response?.data?.message || 'Failed to delete event. Please try again.');
      setDeleteDialogOpen(false);
    } finally {
      setDeleting(false);
    }
  };

  const isUserRegistered = () => {
    if (!isAuthenticated || !event || !event.registrations) return false;
    return event.registrations.some(reg => reg.userId === currentUser.id);
  };

  const isOrganizer = () => {
    if (!isAuthenticated || !event) return false;
    return event.organizer.id === currentUser.id || hasRole('ADMIN');
  };

  const getDefaultImage = (category) => {
    const images = {
      'CONFERENCE': 'https://images.unsplash.com/photo-1505373877841-8d25f7d46678',
      'WORKSHOP': 'https://images.unsplash.com/photo-1552664730-d307ca884978',
      'SEMINAR': 'https://images.unsplash.com/photo-1540575467063-178a50c2df87',
      'NETWORKING': 'https://images.unsplash.com/photo-1528605248644-14dd04022da1',
      'HACKATHON': 'https://images.unsplash.com/photo-1504384308090-c894fdcc538d',
      'CULTURAL': 'https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3',
      'SPORTS': 'https://images.unsplash.com/photo-1461896836934-ffe607ba8211',
      'OTHER': 'https://images.unsplash.com/photo-1523580494863-6f3031224c94'
    };
    return images[category] || images['OTHER'];
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
        <Button variant="contained" onClick={() => navigate('/')}>
          Back to Events
        </Button>
      </Container>
    );
  }

  if (!event) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="info">
          Event not found.
        </Alert>
        <Button variant="contained" onClick={() => navigate('/')} sx={{ mt: 2 }}>
          Back to Events
        </Button>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Grid container spacing={4}>
        <Grid item xs={12} md={8}>
          <Card>
            <CardMedia
              component="img"
              height="300"
              image={event.imageUrl || getDefaultImage(event.category)}
              alt={event.title}
            />
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
                <Box>
                  <Typography variant="h4" component="h1" gutterBottom>
                    {event.title}
                  </Typography>
                  <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                    <Chip 
                      label={event.category.charAt(0) + event.category.slice(1).toLowerCase()} 
                      color="primary" 
                      variant="outlined"
                    />
                    <Chip 
                      label={event.status} 
                      color={event.status === 'UPCOMING' ? 'success' : 'default'}
                      variant="outlined"
                    />
                  </Box>
                </Box>
                {isOrganizer() && (
                  <Box>
                    <Button 
                      startIcon={<Edit />} 
                      variant="outlined" 
                      onClick={handleEditEvent}
                      sx={{ mr: 1 }}
                    >
                      Edit
                    </Button>
                    <Button 
                      startIcon={<Delete />} 
                      variant="outlined" 
                      color="error"
                      onClick={() => setDeleteDialogOpen(true)}
                    >
                      Delete
                    </Button>
                  </Box>
                )}
              </Box>

              <Box sx={{ mb: 3 }}>
                <Tabs value={tabValue} onChange={handleTabChange} aria-label="event details tabs">
                  <Tab label="Overview" id="tab-0" />
                  <Tab label="Agenda" id="tab-1" />
                  <Tab label="Speakers" id="tab-2" />
                  <Tab label="Tickets" id="tab-3" />
                </Tabs>
              </Box>

              <Box role="tabpanel" hidden={tabValue !== 0}>
                {tabValue === 0 && (
                  <>
                    <Typography variant="body1" paragraph>
                      {event.description}
                    </Typography>

                    <Grid container spacing={2} sx={{ mt: 2 }}>
                      <Grid item xs={12} sm={6}>
                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                          <LocationOn color="primary" sx={{ mr: 1 }} />
                          <Typography variant="body1">
                            {event.location}
                          </Typography>
                        </Box>
                      </Grid>
                      <Grid item xs={12} sm={6}>
                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                          <CalendarToday color="primary" sx={{ mr: 1 }} />
                          <Typography variant="body1">
                            {formatDateRange(event.startDate, event.endDate)}
                          </Typography>
                        </Box>
                      </Grid>
                      <Grid item xs={12} sm={6}>
                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                          <Person color="primary" sx={{ mr: 1 }} />
                          <Typography variant="body1">
                            Organized by: {event.organizer.firstName} {event.organizer.lastName}
                          </Typography>
                        </Box>
                      </Grid>
                    </Grid>

                    {event.additionalInfo && (
                      <Box sx={{ mt: 3 }}>
                        <Typography variant="h6" gutterBottom>
                          Additional Information
                        </Typography>
                        <Typography variant="body1">
                          {event.additionalInfo}
                        </Typography>
                      </Box>
                    )}
                  </>
                )}
              </Box>

              <Box role="tabpanel" hidden={tabValue !== 1}>
                {tabValue === 1 && (
                  <Box sx={{ mt: 2 }}>
                    {event.agenda && event.agenda.length > 0 ? (
                      <List>
                        {event.agenda.map((item, index) => (
                          <React.Fragment key={index}>
                            <ListItem alignItems="flex-start">
                              <ListItemIcon>
                                <Schedule color="primary" />
                              </ListItemIcon>
                              <ListItemText
                                primary={
                                  <Typography variant="subtitle1">
                                    {item.title}
                                  </Typography>
                                }
                                secondary={
                                  <>
                                    <Typography variant="body2" color="text.primary">
                                      {formatDate(item.startTime)} - {formatDate(item.endTime)}
                                    </Typography>
                                    <Typography variant="body2" sx={{ mt: 1 }}>
                                      {item.description}
                                    </Typography>
                                  </>
                                }
                              />
                            </ListItem>
                            {index < event.agenda.length - 1 && <Divider variant="inset" component="li" />}
                          </React.Fragment>
                        ))}
                      </List>
                    ) : (
                      <Typography variant="body1" color="text.secondary">
                        No agenda items available for this event.
                      </Typography>
                    )}
                  </Box>
                )}
              </Box>

              <Box role="tabpanel" hidden={tabValue !== 2}>
                {tabValue === 2 && (
                  <Box sx={{ mt: 2 }}>
                    {event.speakers && event.speakers.length > 0 ? (
                      <Grid container spacing={3}>
                        {event.speakers.map((speaker, index) => (
                          <Grid item xs={12} sm={6} md={4} key={index}>
                            <Card variant="outlined">
                              <CardContent>
                                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                  <Avatar
                                    sx={{ width: 60, height: 60, mr: 2, bgcolor: 'primary.main' }}
                                  >
                                    {speaker.name.charAt(0)}
                                  </Avatar>
                                  <Box>
                                    <Typography variant="h6">{speaker.name}</Typography>
                                    <Typography variant="body2" color="text.secondary">
                                      {speaker.title}
                                    </Typography>
                                  </Box>
                                </Box>
                                <Typography variant="body2">
                                  {speaker.bio}
                                </Typography>
                              </CardContent>
                            </Card>
                          </Grid>
                        ))}
                      </Grid>
                    ) : (
                      <Typography variant="body1" color="text.secondary">
                        No speaker information available for this event.
                      </Typography>
                    )}
                  </Box>
                )}
              </Box>

              <Box role="tabpanel" hidden={tabValue !== 3}>
                {tabValue === 3 && (
                  <Box sx={{ mt: 2 }}>
                    {event.ticketTypes && event.ticketTypes.length > 0 ? (
                      <Grid container spacing={2}>
                        {event.ticketTypes.map((ticket, index) => (
                          <Grid item xs={12} sm={6} key={index}>
                            <Paper variant="outlined" sx={{ p: 2 }}>
                              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                                <Typography variant="h6">{ticket.name}</Typography>
                                <Chip 
                                  label={`$${ticket.price.toFixed(2)}`} 
                                  color="primary" 
                                  variant={ticket.price === 0 ? "outlined" : "filled"}
                                />
                              </Box>
                              <Typography variant="body2" paragraph>
                                {ticket.description}
                              </Typography>
                              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                <ConfirmationNumber fontSize="small" sx={{ mr: 1 }} />
                                <Typography variant="body2" color="text.secondary">
                                  {ticket.quantityAvailable === -1 
                                    ? 'Unlimited tickets available' 
                                    : `${ticket.quantityAvailable} tickets available`}
                                </Typography>
                              </Box>
                            </Paper>
                          </Grid>
                        ))}
                      </Grid>
                    ) : (
                      <Typography variant="body1" color="text.secondary">
                        No ticket information available for this event.
                      </Typography>
                    )}
                  </Box>
                )}
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Registration
              </Typography>
              
              {registerError && (
                <Alert severity="error" sx={{ mb: 2 }}>
                  {registerError}
                </Alert>
              )}
              
              {isUserRegistered() ? (
                <Alert severity="success" sx={{ mb: 2 }}>
                  You are registered for this event!
                </Alert>
              ) : (
                <Button
                  variant="contained"
                  color="primary"
                  fullWidth
                  disabled={registering || event.status !== 'UPCOMING'}
                  onClick={handleRegisterForEvent}
                >
                  {registering ? <CircularProgress size={24} /> : 'Register Now'}
                </Button>
              )}
              
              {event.status !== 'UPCOMING' && (
                <Typography variant="body2" color="error" sx={{ mt: 1, textAlign: 'center' }}>
                  Registration is closed for this event
                </Typography>
              )}
              
              <Box sx={{ mt: 3 }}>
                <Typography variant="subtitle2" gutterBottom>
                  Event Details:
                </Typography>
                <List dense>
                  <ListItem>
                    <ListItemIcon>
                      <CalendarToday fontSize="small" />
                    </ListItemIcon>
                    <ListItemText 
                      primary="Date & Time" 
                      secondary={formatDateRange(event.startDate, event.endDate)} 
                    />
                  </ListItem>
                  <ListItem>
                    <ListItemIcon>
                      <LocationOn fontSize="small" />
                    </ListItemIcon>
                    <ListItemText 
                      primary="Location" 
                      secondary={event.location} 
                    />
                  </ListItem>
                  <ListItem>
                    <ListItemIcon>
                      <Info fontSize="small" />
                    </ListItemIcon>
                    <ListItemText 
                      primary="Category" 
                      secondary={event.category.charAt(0) + event.category.slice(1).toLowerCase()} 
                    />
                  </ListItem>
                </List>
              </Box>
            </CardContent>
          </Card>
          
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Organizer
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <Avatar sx={{ bgcolor: 'primary.main', mr: 2 }}>
                  {event.organizer.firstName.charAt(0)}
                </Avatar>
                <Typography>
                  {event.organizer.firstName} {event.organizer.lastName}
                </Typography>
              </Box>
              <Typography variant="body2" color="text.secondary">
                Contact the organizer for any questions about this event.
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Dialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        aria-labelledby="alert-dialog-title"
        aria-describedby="alert-dialog-description"
      >
        <DialogTitle id="alert-dialog-title">
          Delete Event
        </DialogTitle>
        <DialogContent>
          <DialogContentText id="alert-dialog-description">
            Are you sure you want to delete this event? This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)} disabled={deleting}>
            Cancel
          </Button>
          <Button onClick={handleDeleteEvent} color="error" autoFocus disabled={deleting}>
            {deleting ? <CircularProgress size={24} /> : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default EventDetail;