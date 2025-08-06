import React, { useState, useEffect, useRef } from 'react';
import {
  Box,
  Typography,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Badge,
  Popover,
  Divider,
  Button,
  CircularProgress
} from '@mui/material';
import {
  Notifications as NotificationsIcon,
  MarkEmailRead as MarkReadIcon
} from '@mui/icons-material';
import { getUserNotifications, markNotificationAsRead } from '../../services/userService';
import { formatDate } from '../../utils/dateUtils';

const NotificationCenter = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [anchorEl, setAnchorEl] = useState(null);
  const notificationIconRef = useRef(null);

  useEffect(() => {
    fetchNotifications();
    // Set up polling for new notifications every minute
    const intervalId = setInterval(fetchNotifications, 60000);
    
    return () => clearInterval(intervalId);
  }, []);

  const fetchNotifications = async () => {
    try {
      setLoading(true);
      const data = await getUserNotifications();
      setNotifications(data);
      setError(null);
    } catch (err) {
      console.error('Error fetching notifications:', err);
      setError('Failed to load notifications');
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAsRead = async (notificationId, event) => {
    event.stopPropagation();
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

  const handleMarkAllAsRead = async () => {
    try {
      const unreadNotifications = notifications.filter(notification => !notification.read);
      await Promise.all(
        unreadNotifications.map(notification => 
          markNotificationAsRead(notification.id)
        )
      );
      setNotifications(notifications.map(notification => ({ ...notification, read: true })));
    } catch (err) {
      console.error('Error marking all notifications as read:', err);
    }
  };

  const handleClick = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleNotificationClick = (notification) => {
    // If the notification has a link, navigate to it
    if (notification.link) {
      window.location.href = notification.link;
    }
    
    // Mark as read if not already read
    if (!notification.read) {
      markNotificationAsRead(notification.id);
      setNotifications(notifications.map(n => 
        n.id === notification.id ? { ...n, read: true } : n
      ));
    }
    
    handleClose();
  };

  const unreadCount = notifications.filter(notification => !notification.read).length;
  const open = Boolean(anchorEl);
  const id = open ? 'notification-popover' : undefined;

  return (
    <Box>
      <IconButton 
        color="inherit" 
        onClick={handleClick}
        ref={notificationIconRef}
        aria-describedby={id}
      >
        <Badge badgeContent={unreadCount} color="error">
          <NotificationsIcon />
        </Badge>
      </IconButton>
      
      <Popover
        id={id}
        open={open}
        anchorEl={anchorEl}
        onClose={handleClose}
        anchorOrigin={{
          vertical: 'bottom',
          horizontal: 'right',
        }}
        transformOrigin={{
          vertical: 'top',
          horizontal: 'right',
        }}
        PaperProps={{
          sx: { width: 320, maxHeight: 400 }
        }}
      >
        <Box sx={{ p: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6">Notifications</Typography>
          {unreadCount > 0 && (
            <Button 
              size="small" 
              onClick={handleMarkAllAsRead}
              startIcon={<MarkReadIcon />}
            >
              Mark all as read
            </Button>
          )}
        </Box>
        
        <Divider />
        
        {loading && (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
            <CircularProgress size={24} />
          </Box>
        )}
        
        {error && (
          <Box sx={{ p: 2 }}>
            <Typography color="error">{error}</Typography>
          </Box>
        )}
        
        {!loading && !error && notifications.length === 0 && (
          <Box sx={{ p: 2 }}>
            <Typography variant="body2" color="text.secondary" align="center">
              You have no notifications
            </Typography>
          </Box>
        )}
        
        {!loading && !error && notifications.length > 0 && (
          <List sx={{ p: 0 }}>
            {notifications.map((notification) => (
              <React.Fragment key={notification.id}>
                <ListItem 
                  button 
                  alignItems="flex-start"
                  onClick={() => handleNotificationClick(notification)}
                  sx={{
                    bgcolor: notification.read ? 'transparent' : 'rgba(0, 0, 0, 0.04)',
                    '&:hover': { bgcolor: 'rgba(0, 0, 0, 0.08)' }
                  }}
                >
                  <ListItemText
                    primary={
                      <Typography 
                        variant="subtitle2" 
                        component="span"
                        sx={{ fontWeight: notification.read ? 'normal' : 'bold' }}
                      >
                        {notification.title}
                      </Typography>
                    }
                    secondary={
                      <React.Fragment>
                        <Typography variant="body2" component="span" display="block">
                          {notification.message}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {formatDate(notification.createdAt)}
                        </Typography>
                      </React.Fragment>
                    }
                  />
                  {!notification.read && (
                    <ListItemSecondaryAction>
                      <IconButton 
                        edge="end" 
                        aria-label="mark as read"
                        onClick={(e) => handleMarkAsRead(notification.id, e)}
                        size="small"
                      >
                        <MarkReadIcon fontSize="small" />
                      </IconButton>
                    </ListItemSecondaryAction>
                  )}
                </ListItem>
                <Divider component="li" />
              </React.Fragment>
            ))}
          </List>
        )}
        
        <Box sx={{ p: 1, display: 'flex', justifyContent: 'center' }}>
          <Button 
            size="small" 
            onClick={() => {
              handleClose();
              window.location.href = '/profile';
            }}
          >
            View All Notifications
          </Button>
        </Box>
      </Popover>
    </Box>
  );
};

export default NotificationCenter;