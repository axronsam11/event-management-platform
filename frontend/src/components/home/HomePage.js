import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Container,
  Grid,
  Card,
  CardContent,
  CardMedia,
  Typography,
  Button,
  Box,
  Chip,
  TextField,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Pagination,
  CircularProgress,
  Alert
} from '@mui/material';
import { Search as SearchIcon, LocationOn, CalendarToday } from '@mui/icons-material';
import { getEvents } from '../../services/eventService';
import { formatDate } from '../../utils/dateUtils';

const HomePage = () => {
  const navigate = useNavigate();
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [category, setCategory] = useState('');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  const categories = [
    'CONFERENCE',
    'WORKSHOP',
    'SEMINAR',
    'NETWORKING',
    'HACKATHON',
    'CULTURAL',
    'SPORTS',
    'OTHER'
  ];

  useEffect(() => {
    fetchEvents();
  }, [page, category]);

  const fetchEvents = async () => {
    try {
      setLoading(true);
      const response = await getEvents(page - 1, 9, { category, search: searchTerm });
      setEvents(response.content);
      setTotalPages(response.totalPages);
    } catch (err) {
      console.error('Error fetching events:', err);
      setError('Failed to load events. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(1); // Reset to first page when searching
    fetchEvents();
  };

  const handleCategoryChange = (e) => {
    setCategory(e.target.value);
    setPage(1); // Reset to first page when changing category
  };

  const handlePageChange = (event, value) => {
    setPage(value);
  };

  const handleViewEvent = (eventId) => {
    navigate(`/events/${eventId}`);
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

  return (
    <Container maxWidth="lg">
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Discover Events
        </Typography>
        <Typography variant="subtitle1" color="text.secondary" paragraph>
          Find and join exciting events happening around you
        </Typography>
      </Box>

      <Box sx={{ mb: 4 }}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} sm={6} md={4}>
            <form onSubmit={handleSearch}>
              <TextField
                fullWidth
                placeholder="Search events..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                InputProps={{
                  endAdornment: (
                    <InputAdornment position="end">
                      <Button type="submit" sx={{ minWidth: 'auto' }}>
                        <SearchIcon />
                      </Button>
                    </InputAdornment>
                  ),
                }}
              />
            </form>
          </Grid>
          <Grid item xs={12} sm={6} md={4}>
            <FormControl fullWidth>
              <InputLabel id="category-select-label">Category</InputLabel>
              <Select
                labelId="category-select-label"
                id="category-select"
                value={category}
                label="Category"
                onChange={handleCategoryChange}
              >
                <MenuItem value="">All Categories</MenuItem>
                {categories.map((cat) => (
                  <MenuItem key={cat} value={cat}>
                    {cat.charAt(0) + cat.slice(1).toLowerCase()}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 4 }}>
          {error}
        </Alert>
      )}

      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', my: 4 }}>
          <CircularProgress />
        </Box>
      ) : events.length === 0 ? (
        <Box sx={{ textAlign: 'center', my: 4 }}>
          <Typography variant="h6" color="text.secondary">
            No events found. Try adjusting your search criteria.
          </Typography>
        </Box>
      ) : (
        <>
          <Grid container spacing={4}>
            {events.map((event) => (
              <Grid item key={event.id} xs={12} sm={6} md={4}>
                <Card 
                  sx={{ 
                    height: '100%', 
                    display: 'flex', 
                    flexDirection: 'column',
                    transition: 'transform 0.3s',
                    '&:hover': {
                      transform: 'translateY(-5px)',
                      boxShadow: 6
                    }
                  }}
                >
                  <CardMedia
                    component="img"
                    height="140"
                    image={event.imageUrl || getDefaultImage(event.category)}
                    alt={event.title}
                  />
                  <CardContent sx={{ flexGrow: 1 }}>
                    <Box sx={{ mb: 1 }}>
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
                        sx={{ ml: 1 }}
                      />
                    </Box>
                    <Typography gutterBottom variant="h6" component="div">
                      {event.title}
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                      <LocationOn fontSize="small" color="action" />
                      <Typography variant="body2" color="text.secondary" sx={{ ml: 0.5 }}>
                        {event.location}
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                      <CalendarToday fontSize="small" color="action" />
                      <Typography variant="body2" color="text.secondary" sx={{ ml: 0.5 }}>
                        {formatDate(event.startDate)}
                      </Typography>
                    </Box>
                    <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                      {event.description.length > 100
                        ? `${event.description.substring(0, 100)}...`
                        : event.description}
                    </Typography>
                    <Button 
                      variant="contained" 
                      fullWidth
                      onClick={() => handleViewEvent(event.id)}
                    >
                      View Details
                    </Button>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
          
          <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
            <Pagination 
              count={totalPages} 
              page={page} 
              onChange={handlePageChange} 
              color="primary" 
            />
          </Box>
        </>
      )}
    </Container>
  );
};

export default HomePage;