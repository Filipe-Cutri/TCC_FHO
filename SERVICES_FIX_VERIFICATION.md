# Services Display Fix - Verification Guide

## Problem Fixed
Services were not displaying on the client-services.html page despite being properly registered in the database and linked to the establishment.

## Root Cause
1. Missing JavaScript dependencies in client-services.html (`api-config.js` and `client-session.js`)
2. Backend compilation error (duplicate endpoint in AppointmentController)

## Changes Made

### Backend
- **File**: `back-end/src/main/java/com/slotfy/controller/AppointmentController.java`
- **Change**: Removed duplicate `confirmAppointment` method at line 615-639
- **Reason**: Two methods were mapped to the same endpoint causing Spring Boot to fail on startup

### Frontend
- **File**: `front-end/src/pages/client/client-services.html`
  - **Added**: `<script src="../../assets/js/api-config.js"></script>`
  - **Added**: `<script src="../../assets/js/client-session.js"></script>`
  
- **File**: `front-end/src/pages/client/client-dashboard.html`
  - **Added**: `<script src="../../assets/js/api-config.js"></script>`

## How to Verify the Fix

### 1. Start Backend Server
```bash
cd back-end
./gradlew build -x test
java -jar build/libs/slotify-backend-0.0.1-SNAPSHOT.jar
```

Backend should start successfully on port 8443 (HTTPS).

### 2. Create Test Data
Run the following script to create test establishment, client, services, and professionals:

```bash
BASE_URL="https://localhost:8443"

# Create Establishment
curl -k -X POST "${BASE_URL}/api/establishment/profile" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Barbearia Premium Centro",
    "email": "contato@barbpremium.com",
    "phone": "(11) 99999-9999",
    "category": "barbearia",
    "address": "Rua das Flores, 123",
    "description": "A melhor barbearia do centro"
  }'

# Create Client
curl -k -X POST "${BASE_URL}/api/client/register" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João da Silva",
    "email": "joao@cliente.com",
    "password": "senha123",
    "phone": "(11) 98888-8888"
  }'

# Link Client to Establishment (use IDs from responses above)
curl -k -X PUT "${BASE_URL}/api/client/establishment" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": 1,
    "establishmentId": 1
  }'

# Create Services
curl -k -X POST "${BASE_URL}/api/establishment/services" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Corte Masculino",
    "description": "Corte de cabelo masculino profissional",
    "durationMinutes": 30,
    "price": 40.00,
    "establishmentId": 1,
    "category": "corte"
  }'

curl -k -X POST "${BASE_URL}/api/establishment/services" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Barba",
    "description": "Aparar e modelar barba",
    "durationMinutes": 20,
    "price": 25.00,
    "establishmentId": 1,
    "category": "barba"
  }'

curl -k -X POST "${BASE_URL}/api/establishment/services" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Corte + Barba",
    "description": "Combo completo",
    "durationMinutes": 50,
    "price": 60.00,
    "establishmentId": 1,
    "category": "combo"
  }'

# Create Professional
curl -k -X POST "${BASE_URL}/api/establishment/professionals" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Carlos Silva",
    "email": "carlos@barbpremium.com",
    "phone": "(11) 97777-7777",
    "specialties": "Corte, Barba",
    "establishmentId": 1
  }'
```

### 3. Verify API Endpoints
```bash
# Login as client
curl -k -X POST "${BASE_URL}/api/client/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@cliente.com",
    "password": "senha123"
  }'

# Get services for establishment
curl -k "${BASE_URL}/api/client/establishments/1/services"

# Get professionals for establishment
curl -k "${BASE_URL}/api/client/establishments/1/professionals"
```

Expected results:
- Login should return `selectedEstablishmentId: 1`
- Services endpoint should return 3 services
- Professionals endpoint should return 1+ professionals

### 4. Test Frontend

1. Start a web server for the frontend:
```bash
cd front-end/src
python3 -m http.server 8080
```

2. Open browser to: `http://localhost:8080/pages/client/client-login.html`

3. Login with:
   - Email: `joao@cliente.com`
   - Password: `senha123`

4. Navigate to: `http://localhost:8080/pages/client/client-services.html`

5. Verify:
   - ✅ Services should be displayed (3 service cards)
   - ✅ Each service shows: name, description, duration, and price
   - ✅ "Agendar" button should be visible for each service
   - ✅ AI Scheduling section should be visible at the top

### 5. Test Manual Booking Flow

1. Click "Agendar" on any service
2. A modal should open with:
   - Selected service information
   - Professional selection dropdown
   - Date picker
   - Time slot selection
   - Notes textarea

3. Select a professional, date, and time
4. Click "Confirmar Agendamento"
5. Should see success message and redirect to bookings page

## API Endpoints Reference

### Client Endpoints
- `POST /api/client/login` - Client login
- `POST /api/client/register` - Client registration
- `PUT /api/client/establishment` - Link client to establishment
- `GET /api/client/establishments/{id}` - Get establishment details
- `GET /api/client/establishments/{id}/services` - Get establishment services
- `GET /api/client/establishments/{id}/professionals` - Get establishment professionals
- `POST /api/client/appointments/book` - Book appointment

### Establishment Endpoints
- `POST /api/establishment/profile` - Create establishment
- `POST /api/establishment/services` - Create service
- `POST /api/establishment/professionals` - Create professional

## Test Data Created

### Establishment
- ID: 1
- Name: Barbearia Premium Centro
- Category: barbearia
- Status: ACTIVE

### Client
- ID: 1 (or 2 depending on order)
- Name: João da Silva
- Email: joao@cliente.com
- Password: senha123
- Selected Establishment ID: 1

### Services
1. Corte Masculino - R$ 40.00 - 30 min
2. Barba - R$ 25.00 - 20 min
3. Corte + Barba - R$ 60.00 - 50 min

### Professionals
1. Carlos Silva - Specialties: Corte, Barba

## Troubleshooting

### Services not appearing
1. Check browser console for JavaScript errors
2. Verify establishment ID is set in session:
   ```javascript
   console.log(sessionStorage.getItem('selectedEstablishmentId'));
   console.log(JSON.parse(localStorage.getItem('slotfy_client_session')));
   ```
3. Check if API returns services:
   ```bash
   curl -k "https://localhost:8443/api/client/establishments/1/services"
   ```

### Backend not starting
1. Check for duplicate endpoint mappings
2. Verify port 8443 is not in use
3. Check logs for specific errors

### Login issues
1. Verify client exists in database
2. Check password is correct (senha123)
3. Verify backend is running

## Security Notes

- Backend uses HTTPS (self-signed certificate in development)
- Use `-k` flag with curl to bypass certificate validation in development
- In production, use proper SSL certificates
- Passwords should be hashed (BCrypt) in production

## Next Steps

After verification:
1. Test complete booking flow end-to-end
2. Verify AI scheduling recommendations work
3. Test with multiple establishments
4. Add more comprehensive error handling
5. Add loading states and better UX feedback
