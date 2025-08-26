# Frontend-Backend Integration Guide

This document explains how the Slotfy frontend is integrated with the backend APIs.

## Overview

The frontend has been updated to integrate with the Spring Boot backend through REST APIs. The integration includes:

- Centralized API configuration
- Session management for both clients and establishments
- Form integration with real API endpoints
- Proper error handling and loading states

## API Configuration

### Base URL Detection
The system automatically detects the environment:
- **Development**: `http://localhost:8080` (when running on localhost)
- **Production**: Relative URLs (when deployed)

### API Endpoints
All endpoints are centrally configured in `/front-end/src/assets/js/api-config.js`:

#### Client APIs
- `POST /api/client/login` - Client authentication
- `POST /api/client/register` - Client registration
- `POST /api/client/forgot-password` - Password reset

#### Establishment APIs
- `POST /api/establishment/login` - Establishment authentication
- `POST /api/establishment/register` - Establishment registration
- `GET /api/establishment/dashboard/*` - Dashboard data
- `GET /api/establishment/appointments/*` - Appointment management
- `GET /api/establishment/services/*` - Service management

## Session Management

### Client Sessions
- **File**: `/front-end/src/assets/js/client-session.js`
- **Storage**: localStorage with 24-hour expiration
- **Key**: `slotfy_client_session`

### Establishment Sessions
- **File**: `/front-end/src/assets/js/establishment-session.js`
- **Storage**: localStorage with 24-hour expiration
- **Key**: `slotfy_establishment_session`

## Form Integration

### Client Login (`/front-end/src/pages/client/client-login.html`)
- Calls `POST /api/client/login`
- Creates client session on success
- Redirects to client dashboard

### Client Registration (`/front-end/src/pages/client/client-register.html`)
- Calls `POST /api/client/register`
- Creates client session on success
- Redirects to preferences setup

### Establishment Login (`/front-end/src/pages/establishment/establishment-login.html`)
- Calls `POST /api/establishment/login`
- Creates establishment session on success
- Redirects to establishment dashboard

## Testing the Integration

### Starting the Backend
```bash
cd back-end
# For development with H2 database
SPRING_PROFILES_ACTIVE=test ./gradlew bootRun

# For production with PostgreSQL
./gradlew bootRun
```

### Starting the Frontend
```bash
cd front-end
python3 -m http.server 3000
```

### Testing API Calls
1. Open `http://localhost:3000/src/index.html`
2. Click "Sou Cliente" to test client login
3. Fill the form and submit - should call the API
4. Register new accounts to test registration API

## API Request Format

### Client Login
```json
POST /api/client/login
{
  "email": "user@example.com",
  "password": "password123"
}
```

### Client Registration
```json
POST /api/client/register
{
  "name": "João Silva",
  "email": "joao@example.com",
  "phone": "(11) 99999-9999",
  "password": "password123"
}
```

### Establishment Login
```json
POST /api/establishment/login
{
  "email": "admin@establishment.com",
  "password": "password123",
  "role": "admin"
}
```

## Error Handling

The integration includes comprehensive error handling:

- **Network errors**: "Failed to fetch" when backend is down
- **API errors**: Displays server error messages
- **Validation errors**: Client-side validation before API calls
- **Loading states**: Visual feedback during API requests

## CORS Configuration

The backend is configured to accept requests from:
- `http://localhost:3000` (frontend development server)
- Origin patterns can be updated in backend controllers

## Environment Variables

No environment variables are required. The system automatically detects:
- Development vs production environment
- API base URL based on current hostname
- Session storage and expiration

## Troubleshooting

### Common Issues

1. **"Failed to fetch" error**
   - Ensure backend is running on port 8080
   - Check CORS configuration
   - Verify API endpoints are correct

2. **Session not persisting**
   - Check localStorage is enabled
   - Verify session expiration (24 hours)
   - Clear localStorage if corrupted

3. **API endpoint not found**
   - Verify backend routes are correctly implemented
   - Check API URL configuration
   - Ensure controller mappings match frontend calls

### Debug Console

Check browser console for:
- API request URLs
- Request/response data
- JavaScript errors
- Network failures

All API calls are logged for debugging purposes.