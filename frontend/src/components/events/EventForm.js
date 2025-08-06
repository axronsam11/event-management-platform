import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useFormik, FieldArray, FormikProvider } from 'formik';
import * as Yup from 'yup';
import {
  Container,
  Box,
  Typography,
  TextField,
  Button,
  Grid,
  Paper,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  IconButton,
  Divider,
  Card,
  CardContent,
  Alert,
  CircularProgress
} from '@mui/material';
import { Add as AddIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { createEvent, getEventById, updateEvent } from '../../services/eventService';
import { useAuth } from '../../contexts/AuthContext';

const EventForm = () => {
  const { eventId } = useParams();
  const navigate = useNavigate();
  const { hasRole } = useAuth();
  const [loading, setLoading] = useState(eventId ? true : false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const isEditMode = Boolean(eventId);

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

  const validationSchema = Yup.object({
    title: Yup.string().required('Title is required').max(100, 'Title must be at most 100 characters'),
    description: Yup.string().required('Description is required'),
    location: Yup.string().required('Location is required'),
    startDate: Yup.date().required('Start date is required')
      .min(new Date(), 'Start date must be in the future'),
    endDate: Yup.date().required('End date is required')
      .min(Yup.ref('startDate'), 'End date must be after start date'),
    category: Yup.string().required('Category is required'),
    imageUrl: Yup.string().url('Must be a valid URL').nullable(),
    additionalInfo: Yup.string().nullable(),
    speakers: Yup.array().of(
      Yup.object({
        name: Yup.string().required('Speaker name is required'),
        title: Yup.string().required('Speaker title is required'),
        bio: Yup.string().nullable()
      })
    ),
    agenda: Yup.array().of(
      Yup.object({
        title: Yup.string().required('Agenda item title is required'),
        description: Yup.string().nullable(),
        startTime: Yup.date().required('Start time is required'),
        endTime: Yup.date().required('End time is required')
          .min(Yup.ref('startTime'), 'End time must be after start time')
      })
    ),
    ticketTypes: Yup.array().of(
      Yup.object({
        name: Yup.string().required('Ticket name is required'),
        description: Yup.string().nullable(),
        price: Yup.number().required('Price is required').min(0, 'Price must be non-negative'),
        quantityAvailable: Yup.number().integer('Must be an integer')
          .min(-1, 'Must be -1 (unlimited) or a positive number')
      })
    )
  });

  const formik = useFormik({
    initialValues: {
      title: '',
      description: '',
      location: '',
      startDate: '',
      endDate: '',
      category: '',
      imageUrl: '',
      additionalInfo: '',
      speakers: [],
      agenda: [],
      ticketTypes: []
    },
    validationSchema,
    onSubmit: async (values) => {
      try {
        setSubmitting(true);
        setError('');

        // Format dates for API
        const formattedValues = {
          ...values,
          agenda: values.agenda.map(item => ({
            ...item,
            startTime: new Date(item.startTime).toISOString(),
            endTime: new Date(item.endTime).toISOString()
          })),
          startDate: new Date(values.startDate).toISOString(),
          endDate: new Date(values.endDate).toISOString()
        };

        if (isEditMode) {
          await updateEvent(eventId, formattedValues);
        } else {
          await createEvent(formattedValues);
        }

        navigate('/');
      } catch (err) {
        console.error('Error saving event:', err);
        setError(err.response?.data?.message || 'Failed to save event. Please try again.');
      } finally {
        setSubmitting(false);
      }
    }
  });

  useEffect(() => {
    if (!hasRole('ORGANIZER') && !hasRole('ADMIN')) {
      navigate('/');
      return;
    }

    if (isEditMode) {
      fetchEventDetails();
    }
  }, [eventId]);

  const fetchEventDetails = async () => {
    try {
      setLoading(true);
      const data = await getEventById(eventId);

      // Format dates for form fields
      const formattedData = {
        ...data,
        startDate: formatDateForInput(data.startDate),
        endDate: formatDateForInput(data.endDate),
        agenda: data.agenda?.map(item => ({
          ...item,
          startTime: formatDateForInput(item.startTime),
          endTime: formatDateForInput(item.endTime)
        })) || [],
        speakers: data.speakers || [],
        ticketTypes: data.ticketTypes || []
      };

      formik.setValues(formattedData);
    } catch (err) {
      console.error('Error fetching event details:', err);
      setError('Failed to load event details. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const formatDateForInput = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toISOString().slice(0, 16); // Format as YYYY-MM-DDTHH:MM
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

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Paper elevation={3} sx={{ p: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          {isEditMode ? 'Edit Event' : 'Create New Event'}
        </Typography>

        {error && (
          <Alert severity="error" sx={{ mb: 4 }}>
            {error}
          </Alert>
        )}

        <FormikProvider value={formik}>
          <Box component="form" onSubmit={formik.handleSubmit}>
            <Grid container spacing={3}>
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Basic Information
                </Typography>
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  id="title"
                  name="title"
                  label="Event Title"
                  value={formik.values.title}
                  onChange={formik.handleChange}
                  onBlur={formik.handleBlur}
                  error={formik.touched.title && Boolean(formik.errors.title)}
                  helperText={formik.touched.title && formik.errors.title}
                />
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  id="description"
                  name="description"
                  label="Event Description"
                  multiline
                  rows={4}
                  value={formik.values.description}
                  onChange={formik.handleChange}
                  onBlur={formik.handleBlur}
                  error={formik.touched.description && Boolean(formik.errors.description)}
                  helperText={formik.touched.description && formik.errors.description}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  id="location"
                  name="location"
                  label="Location"
                  value={formik.values.location}
                  onChange={formik.handleChange}
                  onBlur={formik.handleBlur}
                  error={formik.touched.location && Boolean(formik.errors.location)}
                  helperText={formik.touched.location && formik.errors.location}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <FormControl fullWidth>
                  <InputLabel id="category-label">Category</InputLabel>
                  <Select
                    labelId="category-label"
                    id="category"
                    name="category"
                    value={formik.values.category}
                    label="Category"
                    onChange={formik.handleChange}
                    onBlur={formik.handleBlur}
                    error={formik.touched.category && Boolean(formik.errors.category)}
                  >
                    {categories.map((category) => (
                      <MenuItem key={category} value={category}>
                        {category.charAt(0) + category.slice(1).toLowerCase()}
                      </MenuItem>
                    ))}
                  </Select>
                  {formik.touched.category && formik.errors.category && (
                    <Typography variant="caption" color="error">
                      {formik.errors.category}
                    </Typography>
                  )}
                </FormControl>
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  id="startDate"
                  name="startDate"
                  label="Start Date & Time"
                  type="datetime-local"
                  value={formik.values.startDate}
                  onChange={formik.handleChange}
                  onBlur={formik.handleBlur}
                  error={formik.touched.startDate && Boolean(formik.errors.startDate)}
                  helperText={formik.touched.startDate && formik.errors.startDate}
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>

              <Grid item xs={12} sm={6}>
                <TextField
                  fullWidth
                  id="endDate"
                  name="endDate"
                  label="End Date & Time"
                  type="datetime-local"
                  value={formik.values.endDate}
                  onChange={formik.handleChange}
                  onBlur={formik.handleBlur}
                  error={formik.touched.endDate && Boolean(formik.errors.endDate)}
                  helperText={formik.touched.endDate && formik.errors.endDate}
                  InputLabelProps={{ shrink: true }}
                />
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  id="imageUrl"
                  name="imageUrl"
                  label="Image URL"
                  value={formik.values.imageUrl || ''}
                  onChange={formik.handleChange}
                  onBlur={formik.handleBlur}
                  error={formik.touched.imageUrl && Boolean(formik.errors.imageUrl)}
                  helperText={formik.touched.imageUrl && formik.errors.imageUrl}
                />
              </Grid>

              <Grid item xs={12}>
                <TextField
                  fullWidth
                  id="additionalInfo"
                  name="additionalInfo"
                  label="Additional Information"
                  multiline
                  rows={3}
                  value={formik.values.additionalInfo || ''}
                  onChange={formik.handleChange}
                  onBlur={formik.handleBlur}
                  error={formik.touched.additionalInfo && Boolean(formik.errors.additionalInfo)}
                  helperText={formik.touched.additionalInfo && formik.errors.additionalInfo}
                />
              </Grid>

              <Grid item xs={12}>
                <Divider sx={{ my: 2 }} />
                <Typography variant="h6" gutterBottom>
                  Speakers
                </Typography>

                <FieldArray
                  name="speakers"
                  render={arrayHelpers => (
                    <>
                      {formik.values.speakers && formik.values.speakers.length > 0 ? (
                        formik.values.speakers.map((speaker, index) => (
                          <Card key={index} variant="outlined" sx={{ mb: 2 }}>
                            <CardContent>
                              <Grid container spacing={2}>
                                <Grid item xs={12} sm={5}>
                                  <TextField
                                    fullWidth
                                    label="Speaker Name"
                                    name={`speakers.${index}.name`}
                                    value={speaker.name}
                                    onChange={formik.handleChange}
                                    onBlur={formik.handleBlur}
                                    error={formik.touched.speakers?.[index]?.name && 
                                           Boolean(formik.errors.speakers?.[index]?.name)}
                                    helperText={formik.touched.speakers?.[index]?.name && 
                                              formik.errors.speakers?.[index]?.name}
                                  />
                                </Grid>
                                <Grid item xs={12} sm={5}>
                                  <TextField
                                    fullWidth
                                    label="Speaker Title"
                                    name={`speakers.${index}.title`}
                                    value={speaker.title}
                                    onChange={formik.handleChange}
                                    onBlur={formik.handleBlur}
                                    error={formik.touched.speakers?.[index]?.title && 
                                           Boolean(formik.errors.speakers?.[index]?.title)}
                                    helperText={formik.touched.speakers?.[index]?.title && 
                                              formik.errors.speakers?.[index]?.title}
                                  />
                                </Grid>
                                <Grid item xs={12} sm={2} sx={{ display: 'flex', alignItems: 'center' }}>
                                  <IconButton
                                    onClick={() => arrayHelpers.remove(index)}
                                    color="error"
                                  >
                                    <DeleteIcon />
                                  </IconButton>
                                </Grid>
                                <Grid item xs={12}>
                                  <TextField
                                    fullWidth
                                    label="Speaker Bio"
                                    name={`speakers.${index}.bio`}
                                    value={speaker.bio || ''}
                                    onChange={formik.handleChange}
                                    onBlur={formik.handleBlur}
                                    multiline
                                    rows={2}
                                    error={formik.touched.speakers?.[index]?.bio && 
                                           Boolean(formik.errors.speakers?.[index]?.bio)}
                                    helperText={formik.touched.speakers?.[index]?.bio && 
                                              formik.errors.speakers?.[index]?.bio}
                                  />
                                </Grid>
                              </Grid>
                            </CardContent>
                          </Card>
                        ))
                      ) : null}
                      <Button
                        startIcon={<AddIcon />}
                        variant="outlined"
                        onClick={() => arrayHelpers.push({ name: '', title: '', bio: '' })}
                        sx={{ mt: 1 }}
                      >
                        Add Speaker
                      </Button>
                    </>
                  )}
                />
              </Grid>

              <Grid item xs={12}>
                <Divider sx={{ my: 2 }} />
                <Typography variant="h6" gutterBottom>
                  Agenda
                </Typography>

                <FieldArray
                  name="agenda"
                  render={arrayHelpers => (
                    <>
                      {formik.values.agenda && formik.values.agenda.length > 0 ? (
                        formik.values.agenda.map((item, index) => (
                          <Card key={index} variant="outlined" sx={{ mb: 2 }}>
                            <CardContent>
                              <Grid container spacing={2}>
                                <Grid item xs={12}>
                                  <TextField
                                    fullWidth
                                    label="Agenda Item Title"
                                    name={`agenda.${index}.title`}
                                    value={item.title}
                                    onChange={formik.handleChange}
                                    onBlur={formik.handleBlur}
                                    error={formik.touched.agenda?.[index]?.title && 
                                           Boolean(formik.errors.agenda?.[index]?.title)}
                                    helperText={formik.touched.agenda?.[index]?.title && 
                                              formik.errors.agenda?.[index]?.title}
                                  />
                                </Grid>
                                <Grid item xs={12} sm={5}>
                                  <TextField
                                    fullWidth
                                    label="Start Time"
                                    name={`agenda.${index}.startTime`}
                                    type="datetime-local"
                                    value={item.startTime}
                                    onChange={formik.handleChange}
                                    onBlur={formik.handleBlur}
                                    InputLabelProps={{ shrink: true }}
                                    error={formik.touched.agenda?.[index]?.startTime && 
                                           Boolean(formik.errors.agenda?.[index]?.startTime)}
                                    helperText={formik.touched.agenda?.[index]?.startTime && 
                                              formik.errors.agenda?.[index]?.startTime}
                                  />
                                </Grid>
                                <Grid item xs={12} sm={5}>
                                  <TextField
                                    fullWidth
                                    label="End Time"
                                    name={`agenda.${index}.endTime`}
                                    type="datetime-local"
                                    value={item.endTime}
                                    onChange={formik.handleChange}
                                    onBlur={formik.handleBlur}
                                    InputLabelProps={{ shrink: true }}
                                    error={formik.touched.agenda?.[index]?.endTime && 
                                           Boolean(formik.errors.agenda?.[index]?.endTime)}
                                    helperText={formik.touched.agenda?.[index]?.endTime && 
                                              formik.errors.agenda?.[index]?.endTime}
                                  />
                                </Grid>
                                <Grid item xs={12} sm={2} sx={{ display: 'flex', alignItems: 'center' }}>
                                  <IconButton
                                    onClick={() => arrayHelpers.remove(index)}
                                    color="error"
                                  >
                                    <DeleteIcon />
                                  </IconButton>
                                </Grid>
                                <Grid item xs={12}>
                                  <TextField
                                    fullWidth
                                    label="Description"
                                    name={`agenda.${index}.description`}
                                    value={item.description || ''}
                                    onChange={formik.handleChange}
                                    onBlur={formik.handleBlur}
                                    multiline
                                    rows={2}
                                    error={formik.touched.agenda?.[index]?.description && 
                                           Boolean(formik.errors.agenda?.[index]?.description)}
                                    helperText={formik.touched.agenda?.[index]?.description && 
                                              formik.errors.agenda?.[index]?.description}
                                  />
                                </Grid>
                              </Grid>
                            </CardContent>
                          </Card>
                        ))
                      ) : null}
                      <Button
                        startIcon={<AddIcon />}
                        variant="outlined"
                        onClick={() => arrayHelpers.push({ 
                          title: '', 
                          description: '', 
                          startTime: '', 
                          endTime: '' 
                        })}
                        sx={{ mt: 1 }}
                      >
                        Add Agenda Item
                      </Button>
                    </>
                  )}
                />
              </Grid>

              <Grid item xs={12}>
                <Divider sx={{ my: 2 }} />
                <Typography variant="h6" gutterBottom>
                  Ticket Types
                </Typography>

                <FieldArray
                  name="ticketTypes"
                  render={arrayHelpers => (
                    <>
                      {formik.values.ticketTypes && formik.values.ticketTypes.length > 0 ? (
                        formik.values.ticketTypes.map((ticket, index) => (
                          <Card key={index} variant="outlined" sx={{ mb: 2 }}>
                            <CardContent>
                              <Grid container spacing={2}>
                                <Grid item xs={12} sm={4}>
                                  <TextField
                                    fullWidth
                                    label="Ticket Name"
                                    name={`ticketTypes.${index}.name`}
                                    value={ticket.name}
                                    onChange={formik.handleChange}
                                    onBlur={formik.handleBlur}
                                    error={formik.touched.ticketTypes?.[index]?.name && 
                                           Boolean(formik.errors.ticketTypes?.[index]?.name)}
                                    helperText={formik.touched.ticketTypes?.[index]?.name && 
                                              formik.errors.ticketTypes?.[index]?.name}
                                  />
                                </Grid>
                                <Grid item xs={12} sm={3}>
                                  <TextField
                                    fullWidth
                                    label="Price"
                                    name={`ticketTypes.${index}.price`}
                                    type="number"
                                    value={ticket.price}
                                    onChange={formik.handleChange}
                                    onBlur={formik.handleBlur}
                                    InputProps={{ inputProps: { min: 0, step: 0.01 } }}
                                    error={formik.touched.ticketTypes?.[index]?.price && 
                                           Boolean(formik.errors.ticketTypes?.[index]?.price)}
                                    helperText={formik.touched.ticketTypes?.[index]?.price && 
                                              formik.errors.ticketTypes?.[index]?.price}
                                  />
                                </Grid>
                                <Grid item xs={12} sm={3}>
                                  <TextField
                                    fullWidth
                                    label="Quantity Available"
                                    name={`ticketTypes.${index}.quantityAvailable`}
                                    type="number"
                                    value={ticket.quantityAvailable}
                                    onChange={formik.handleChange}
                                    onBlur={formik.handleBlur}
                                    InputProps={{ inputProps: { min: -1 } }}
                                    error={formik.touched.ticketTypes?.[index]?.quantityAvailable && 
                                           Boolean(formik.errors.ticketTypes?.[index]?.quantityAvailable)}
                                    helperText={
                                      (formik.touched.ticketTypes?.[index]?.quantityAvailable && 
                                       formik.errors.ticketTypes?.[index]?.quantityAvailable) ||
                                      "Use -1 for unlimited"
                                    }
                                  />
                                </Grid>
                                <Grid item xs={12} sm={2} sx={{ display: 'flex', alignItems: 'center' }}>
                                  <IconButton
                                    onClick={() => arrayHelpers.remove(index)}
                                    color="error"
                                  >
                                    <DeleteIcon />
                                  </IconButton>
                                </Grid>
                                <Grid item xs={12}>
                                  <TextField
                                    fullWidth
                                    label="Description"
                                    name={`ticketTypes.${index}.description`}
                                    value={ticket.description || ''}
                                    onChange={formik.handleChange}
                                    onBlur={formik.handleBlur}
                                    multiline
                                    rows={2}
                                    error={formik.touched.ticketTypes?.[index]?.description && 
                                           Boolean(formik.errors.ticketTypes?.[index]?.description)}
                                    helperText={formik.touched.ticketTypes?.[index]?.description && 
                                              formik.errors.ticketTypes?.[index]?.description}
                                  />
                                </Grid>
                              </Grid>
                            </CardContent>
                          </Card>
                        ))
                      ) : null}
                      <Button
                        startIcon={<AddIcon />}
                        variant="outlined"
                        onClick={() => arrayHelpers.push({ 
                          name: '', 
                          description: '', 
                          price: 0, 
                          quantityAvailable: -1 
                        })}
                        sx={{ mt: 1 }}
                      >
                        Add Ticket Type
                      </Button>
                    </>
                  )}
                />
              </Grid>

              <Grid item xs={12} sx={{ mt: 3, display: 'flex', justifyContent: 'space-between' }}>
                <Button
                  variant="outlined"
                  onClick={() => navigate('/')}
                  disabled={submitting}
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  variant="contained"
                  disabled={submitting}
                >
                  {submitting ? <CircularProgress size={24} /> : (isEditMode ? 'Update Event' : 'Create Event')}
                </Button>
              </Grid>
            </Grid>
          </Box>
        </FormikProvider>
      </Paper>
    </Container>
  );
};

export default EventForm;