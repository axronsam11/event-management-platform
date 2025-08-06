/**
 * Format a date string to a more readable format
 * @param {string} dateString - ISO date string
 * @returns {string} Formatted date string
 */
export const formatDate = (dateString) => {
  if (!dateString) return '';
  
  const options = { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  };
  
  return new Date(dateString).toLocaleDateString(undefined, options);
};

/**
 * Format a date string to a short format (without time)
 * @param {string} dateString - ISO date string
 * @returns {string} Formatted date string
 */
export const formatShortDate = (dateString) => {
  if (!dateString) return '';
  
  const options = { 
    year: 'numeric', 
    month: 'short', 
    day: 'numeric'
  };
  
  return new Date(dateString).toLocaleDateString(undefined, options);
};

/**
 * Get relative time string (e.g., "2 days ago", "in 3 hours")
 * @param {string} dateString - ISO date string
 * @returns {string} Relative time string
 */
export const getRelativeTimeString = (dateString) => {
  if (!dateString) return '';
  
  const date = new Date(dateString);
  const now = new Date();
  const diffInSeconds = Math.floor((date - now) / 1000);
  const absSeconds = Math.abs(diffInSeconds);
  
  const units = {
    day: 86400,
    hour: 3600,
    minute: 60,
    second: 1
  };
  
  let unit = 'second';
  let value = absSeconds;
  
  for (const [unitName, secondsInUnit] of Object.entries(units)) {
    if (absSeconds >= secondsInUnit) {
      unit = unitName;
      value = Math.floor(absSeconds / secondsInUnit);
      break;
    }
  }
  
  const plural = value !== 1 ? 's' : '';
  return diffInSeconds >= 0 
    ? `in ${value} ${unit}${plural}` 
    : `${value} ${unit}${plural} ago`;
};

/**
 * Format a date range (start date to end date)
 * @param {string} startDateString - ISO date string for start date
 * @param {string} endDateString - ISO date string for end date
 * @returns {string} Formatted date range string
 */
export const formatDateRange = (startDateString, endDateString) => {
  if (!startDateString || !endDateString) return '';
  
  const startDate = new Date(startDateString);
  const endDate = new Date(endDateString);
  
  // Same day event
  if (startDate.toDateString() === endDate.toDateString()) {
    const dateOptions = { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric'
    };
    
    const timeOptions = {
      hour: '2-digit',
      minute: '2-digit'
    };
    
    const datePart = startDate.toLocaleDateString(undefined, dateOptions);
    const startTimePart = startDate.toLocaleTimeString(undefined, timeOptions);
    const endTimePart = endDate.toLocaleTimeString(undefined, timeOptions);
    
    return `${datePart}, ${startTimePart} - ${endTimePart}`;
  }
  
  // Multi-day event
  const options = { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  };
  
  return `${startDate.toLocaleDateString(undefined, options)} - ${endDate.toLocaleDateString(undefined, options)}`;
};