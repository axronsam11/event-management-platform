import React, { useState, useEffect } from 'react';
import {
  Container,
  Grid,
  Paper,
  Typography,
  Box,
  Tabs,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Alert,
  CircularProgress,
  Pagination,
  Tooltip
} from '@mui/material';
import {
  Edit as EditIcon,
  Delete as DeleteIcon,
  Block as BlockIcon,
  CheckCircle as CheckCircleIcon,
  Visibility as ViewIcon,
  Search as SearchIcon
} from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';
import { getAllUsers, getUserById, updateUserRoles } from '../../services/userService';
import { getAllEvents, deleteEvent, updateEventStatus } from '../../services/eventService';
import { formatDate } from '../../utils/dateUtils';

const AdminDashboard = () => {
  const { isAdmin } = useAuth();
  const [tabValue, setTabValue] = useState(0);
  const [users, setUsers] = useState([]);
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [pageSize] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedUser, setSelectedUser] = useState(null);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [openUserDialog, setOpenUserDialog] = useState(false);
  const [openEventDialog, setOpenEventDialog] = useState(false);
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false);
  const [processingAction, setProcessingAction] = useState(false);
  const [userRoles, setUserRoles] = useState([]);

  useEffect(() => {
    if (!isAdmin()) {
      setError('You do not have permission to access this page.');
      return;
    }
    
    if (tabValue === 0) {
      fetchUsers();
    } else {
      fetchEvents();
    }
  }, [tabValue, page, isAdmin]);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await getAllUsers(page - 1, pageSize, searchTerm);
      setUsers(response.content);
      setTotalPages(response.totalPages);
    } catch (err) {
      console.error('Error fetching users:', err);
      setError('Failed to load users. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const fetchEvents = async () => {
    try {
      setLoading(true);
      const response = await getAllEvents(page - 1, pageSize, null, searchTerm);
      setEvents(response.content);
      setTotalPages(response.totalPages);
    } catch (err) {
      console.error('Error fetching events:', err);
      setError('Failed to load events. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
    setPage(1);
    setSearchTerm('');
  };

  const handlePageChange = (event, value) => {
    setPage(value);
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(1);
    if (tabValue === 0) {
      fetchUsers();
    } else {
      fetchEvents();
    }
  };

  const handleOpenUserDialog = async (userId) => {
    try {
      setProcessingAction(true);
      const user = await getUserById(userId);
      setSelectedUser(user);
      setUserRoles([...user.roles]);
      setOpenUserDialog(true);
    } catch (err) {
      console.error('Error fetching user details:', err);
      setError('Failed to load user details. Please try again later.');
    } finally {
      setProcessingAction(false);
    }
  };

  const handleCloseUserDialog = () => {
    setOpenUserDialog(false);
    setSelectedUser(null);
    setUserRoles([]);
  };

  const handleOpenEventDialog = (event) => {
    setSelectedEvent(event);
    setOpenEventDialog(true);
  };

  const handleCloseEventDialog = () => {
    setOpenEventDialog(false);
    setSelectedEvent(null);
  };

  const handleOpenDeleteDialog = (event) => {
    setSelectedEvent(event);
    setOpenDeleteDialog(true);
  };

  const handleCloseDeleteDialog = () => {
    setOpenDeleteDialog(false);
    setSelectedEvent(null);
  };

  const handleRoleChange = (event) => {
    const role = event.target.value;
    if (userRoles.includes(role)) {
      setUserRoles(userRoles.filter(r => r !== role));
    } else {
      setUserRoles([...userRoles, role]);
    }
  };

  const handleUpdateUserRoles = async () => {
    if (!selectedUser) return;
    
    try {
      setProcessingAction(true);
      await updateUserRoles(selectedUser.id, userRoles);
      setUsers(users.map(user => 
        user.id === selectedUser.id ? { ...user, roles: userRoles } : user
      ));
      handleCloseUserDialog();
    } catch (err) {
      console.error('Error updating user roles:', err);
      setError('Failed to update user roles. Please try again later.');
    } finally {
      setProcessingAction(false);
    }
  };

  const handleUpdateEventStatus = async (newStatus) => {
    if (!selectedEvent) return;
    
    try {
      setProcessingAction(true);
      await updateEventStatus(selectedEvent.id, newStatus);
      setEvents(events.map(event => 
        event.id === selectedEvent.id ? { ...event, status: newStatus } : event
      ));
      handleCloseEventDialog();
    } catch (err) {
      console.error('Error updating event status:', err);
      setError('Failed to update event status. Please try again later.');
    } finally {
      setProcessingAction(false);
    }
  };

  const handleDeleteEvent = async () => {
    if (!selectedEvent) return;
    
    try {
      setProcessingAction(true);
      await deleteEvent(selectedEvent.id);
      setEvents(events.filter(event => event.id !== selectedEvent.id));
      handleCloseDeleteDialog();
    } catch (err) {
      console.error('Error deleting event:', err);
      setError('Failed to delete event. Please try again later.');
    } finally {
      setProcessingAction(false);
    }
  };

  if (error && error.includes('permission')) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="error" sx={{ mb: 4 }}>
          {error}
        </Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Admin Dashboard
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 4 }}>
          {error}
        </Alert>
      )}

      <Paper elevation={3} sx={{ p: 3 }}>
        <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
          <Tabs value={tabValue} onChange={handleTabChange} aria-label="admin tabs">
            <Tab label="Manage Users" id="tab-0" />
            <Tab label="Manage Events" id="tab-1" />
          </Tabs>
        </Box>

        <Box sx={{ mb: 3 }}>
          <form onSubmit={handleSearch}>
            <Grid container spacing={2} alignItems="center">
              <Grid item xs={12} sm={6} md={4}>
                <TextField
                  fullWidth
                  label={tabValue === 0 ? "Search Users" : "Search Events"}
                  variant="outlined"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  placeholder={tabValue === 0 ? "Search by name or email" : "Search by title or location"}
                  InputProps={{
                    endAdornment: (
                      <IconButton type="submit" edge="end">
                        <SearchIcon />
                      </IconButton>
                    ),
                  }}
                />
              </Grid>
            </Grid>
          </form>
        </Box>

        <Box role="tabpanel" hidden={tabValue !== 0}>
          {tabValue === 0 && (
            <>
              {loading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
                  <CircularProgress />
                </Box>
              ) : (
                <>
                  <TableContainer>
                    <Table>
                      <TableHead>
                        <TableRow>
                          <TableCell>Name</TableCell>
                          <TableCell>Email</TableCell>
                          <TableCell>Phone</TableCell>
                          <TableCell>Roles</TableCell>
                          <TableCell>Joined Date</TableCell>
                          <TableCell>Actions</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {users.length === 0 ? (
                          <TableRow>
                            <TableCell colSpan={6} align="center">
                              No users found
                            </TableCell>
                          </TableRow>
                        ) : (
                          users.map((user) => (
                            <TableRow key={user.id}>
                              <TableCell>{user.firstName} {user.lastName}</TableCell>
                              <TableCell>{user.email}</TableCell>
                              <TableCell>{user.phoneNumber}</TableCell>
                              <TableCell>
                                {user.roles.map((role, index) => (
                                  <Chip 
                                    key={index} 
                                    label={role} 
                                    color={role === 'ADMIN' ? 'error' : role === 'ORGANIZER' ? 'primary' : 'default'}
                                    size="small"
                                    sx={{ mr: 0.5 }}
                                  />
                                ))}
                              </TableCell>
                              <TableCell>{formatDate(user.createdAt)}</TableCell>
                              <TableCell>
                                <Tooltip title="Edit Roles">
                                  <IconButton 
                                    color="primary" 
                                    onClick={() => handleOpenUserDialog(user.id)}
                                    disabled={processingAction}
                                  >
                                    <EditIcon />
                                  </IconButton>
                                </Tooltip>
                              </TableCell>
                            </TableRow>
                          ))
                        )}
                      </TableBody>
                    </Table>
                  </TableContainer>

                  <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
                    <Pagination 
                      count={totalPages} 
                      page={page} 
                      onChange={handlePageChange} 
                      color="primary" 
                    />
                  </Box>
                </>
              )}
            </>
          )}
        </Box>

        <Box role="tabpanel" hidden={tabValue !== 1}>
          {tabValue === 1 && (
            <>
              {loading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
                  <CircularProgress />
                </Box>
              ) : (
                <>
                  <TableContainer>
                    <Table>
                      <TableHead>
                        <TableRow>
                          <TableCell>Title</TableCell>
                          <TableCell>Organizer</TableCell>
                          <TableCell>Category</TableCell>
                          <TableCell>Date</TableCell>
                          <TableCell>Status</TableCell>
                          <TableCell>Actions</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {events.length === 0 ? (
                          <TableRow>
                            <TableCell colSpan={6} align="center">
                              No events found
                            </TableCell>
                          </TableRow>
                        ) : (
                          events.map((event) => (
                            <TableRow key={event.id}>
                              <TableCell>{event.title}</TableCell>
                              <TableCell>{event.organizer.firstName} {event.organizer.lastName}</TableCell>
                              <TableCell>
                                <Chip 
                                  label={event.category.charAt(0) + event.category.slice(1).toLowerCase()} 
                                  size="small" 
                                  color="primary" 
                                  variant="outlined"
                                />
                              </TableCell>
                              <TableCell>{formatDate(event.startDate)}</TableCell>
                              <TableCell>
                                <Chip 
                                  label={event.status} 
                                  size="small" 
                                  color={
                                    event.status === 'UPCOMING' ? 'success' : 
                                    event.status === 'CANCELLED' ? 'error' : 
                                    event.status === 'COMPLETED' ? 'default' : 'primary'
                                  }
                                />
                              </TableCell>
                              <TableCell>
                                <Tooltip title="View Event">
                                  <IconButton 
                                    color="primary" 
                                    href={`/events/${event.id}`}
                                  >
                                    <ViewIcon />
                                  </IconButton>
                                </Tooltip>
                                <Tooltip title="Change Status">
                                  <IconButton 
                                    color="primary" 
                                    onClick={() => handleOpenEventDialog(event)}
                                    disabled={processingAction}
                                  >
                                    <EditIcon />
                                  </IconButton>
                                </Tooltip>
                                <Tooltip title="Delete Event">
                                  <IconButton 
                                    color="error" 
                                    onClick={() => handleOpenDeleteDialog(event)}
                                    disabled={processingAction}
                                  >
                                    <DeleteIcon />
                                  </IconButton>
                                </Tooltip>
                              </TableCell>
                            </TableRow>
                          ))
                        )}
                      </TableBody>
                    </Table>
                  </TableContainer>

                  <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
                    <Pagination 
                      count={totalPages} 
                      page={page} 
                      onChange={handlePageChange} 
                      color="primary" 
                    />
                  </Box>
                </>
              )}
            </>
          )}
        </Box>
      </Paper>

      {/* User Edit Dialog */}
      <Dialog open={openUserDialog} onClose={handleCloseUserDialog} maxWidth="sm" fullWidth>
        <DialogTitle>Edit User Roles</DialogTitle>
        <DialogContent>
          {selectedUser && (
            <>
              <Typography variant="subtitle1" gutterBottom>
                {selectedUser.firstName} {selectedUser.lastName} ({selectedUser.email})
              </Typography>
              <FormControl fullWidth sx={{ mt: 2 }}>
                <InputLabel id="roles-label">Roles</InputLabel>
                <Select
                  labelId="roles-label"
                  id="roles"
                  multiple
                  value={userRoles}
                  onChange={handleRoleChange}
                  renderValue={(selected) => (
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                      {selected.map((value) => (
                        <Chip 
                          key={value} 
                          label={value} 
                          color={value === 'ADMIN' ? 'error' : value === 'ORGANIZER' ? 'primary' : 'default'}
                        />
                      ))}
                    </Box>
                  )}
                >
                  <MenuItem value="USER">
                    <Chip label="USER" size="small" sx={{ mr: 1 }} /> User
                  </MenuItem>
                  <MenuItem value="ORGANIZER">
                    <Chip label="ORGANIZER" color="primary" size="small" sx={{ mr: 1 }} /> Organizer
                  </MenuItem>
                  <MenuItem value="ADMIN">
                    <Chip label="ADMIN" color="error" size="small" sx={{ mr: 1 }} /> Admin
                  </MenuItem>
                </Select>
              </FormControl>
            </>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseUserDialog}>Cancel</Button>
          <Button 
            onClick={handleUpdateUserRoles} 
            variant="contained" 
            color="primary"
            disabled={processingAction}
          >
            {processingAction ? <CircularProgress size={24} /> : 'Save Changes'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Event Status Dialog */}
      <Dialog open={openEventDialog} onClose={handleCloseEventDialog} maxWidth="sm" fullWidth>
        <DialogTitle>Change Event Status</DialogTitle>
        <DialogContent>
          {selectedEvent && (
            <>
              <Typography variant="subtitle1" gutterBottom>
                {selectedEvent.title}
              </Typography>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Current Status: 
                <Chip 
                  label={selectedEvent.status} 
                  size="small" 
                  color={
                    selectedEvent.status === 'UPCOMING' ? 'success' : 
                    selectedEvent.status === 'CANCELLED' ? 'error' : 
                    selectedEvent.status === 'COMPLETED' ? 'default' : 'primary'
                  }
                  sx={{ ml: 1 }}
                />
              </Typography>
              <Box sx={{ mt: 3, display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Button 
                  variant="outlined" 
                  color="success" 
                  startIcon={<CheckCircleIcon />}
                  onClick={() => handleUpdateEventStatus('UPCOMING')}
                  disabled={selectedEvent.status === 'UPCOMING' || processingAction}
                >
                  Mark as Upcoming
                </Button>
                <Button 
                  variant="outlined" 
                  color="primary" 
                  startIcon={<CheckCircleIcon />}
                  onClick={() => handleUpdateEventStatus('COMPLETED')}
                  disabled={selectedEvent.status === 'COMPLETED' || processingAction}
                >
                  Mark as Completed
                </Button>
                <Button 
                  variant="outlined" 
                  color="error" 
                  startIcon={<BlockIcon />}
                  onClick={() => handleUpdateEventStatus('CANCELLED')}
                  disabled={selectedEvent.status === 'CANCELLED' || processingAction}
                >
                  Mark as Cancelled
                </Button>
              </Box>
            </>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseEventDialog}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Delete Event Dialog */}
      <Dialog open={openDeleteDialog} onClose={handleCloseDeleteDialog}>
        <DialogTitle>Delete Event</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete the event "{selectedEvent?.title}"? This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>Cancel</Button>
          <Button 
            onClick={handleDeleteEvent} 
            variant="contained" 
            color="error"
            disabled={processingAction}
          >
            {processingAction ? <CircularProgress size={24} /> : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default AdminDashboard;