# Image Upload Feature - Implementation Documentation

## Overview
This document describes the implementation of image upload functionality for Professionals, Services, and Establishments in the Slotfy application.

## Features Implemented

### 1. Professional Image Management

#### Backend Changes
- **New Endpoint**: `PUT /api/establishment/professionals/{id}/image`
  - Updates the image URL for a professional
  - Requires `establishmentId` query parameter for security
  - Request body: `{ "imageUrl": "string" }`
  - Response: Professional object with updated imageUrl

- **Service Method**: `ProfessionalService.updateImage(Long professionalId, String imageUrl, Long establishmentId)`
  - Validates professional belongs to the establishment
  - Updates the imageUrl field in the Professional entity

#### Frontend Changes
- **HTML (establishment-professionals.html)**:
  - Added image URL input field to the professional modal
  - Added hidden field for professional ID to support edit mode
  - Added modal title dynamic update for Add/Edit modes
  - Added professional image display in card (80x80 rounded circle)

- **JavaScript (establishment-professionals.js)**:
  - Updated `saveProfessional()` to handle both create and update operations
  - Added `updateProfessionalImage()` method to update image separately
  - Added `editProfessional()` method to populate form with existing data
  - Updated `clearForm()` to reset all fields including image URL
  - Updated `createProfessionalCard()` to display professional images

- **Client View (client-professionals.html)**:
  - Already displays professional images (no changes needed)
  - Shows 100x100 rounded image or default icon

### 2. Service Image Management

#### Backend Changes
- **New Endpoint**: `PUT /api/establishment/services/{id}/image`
  - Updates the image URL for a service
  - Requires `establishmentId` query parameter for security
  - Request body: `{ "imageUrl": "string" }`
  - Response: Service object with updated imageUrl

- **Service Method**: `ServiceService.updateImage(Long serviceId, String imageUrl, Long establishmentId)`
  - Validates service belongs to the establishment
  - Updates the imageUrl field in the Service entity

#### Frontend Changes
- **HTML (establishment-services.html)**:
  - Added image URL input field to the service modal
  - Added hidden field for service ID to support edit mode
  - Added modal title dynamic update for Add/Edit modes

- **JavaScript (establishment-services.js)**:
  - Updated `saveService()` to handle both create and update operations
  - Added `updateServiceImage()` method to update image separately
  - Added `editService()` method to populate form with existing data
  - Updated `clearForm()` to reset all fields including image URL

### 3. Establishment Logo Management

#### Backend Changes
- **Existing Endpoint**: `PUT /api/establishment/profile/{id}/image`
  - Already existed in EstablishmentController
  - Updates the image URL (logo) for an establishment
  - Request body: `{ "imageUrl": "string" }`
  - Response: Establishment object with updated imageUrl

#### Frontend Changes
- **HTML (establishment-admin.html)**:
  - Added logo URL input field to establishment information form
  - Added logo preview section that displays when URL is entered
  - Added form IDs for JavaScript access

- **JavaScript (establishment-admin.html)**:
  - Added `loadEstablishmentData()` to fetch and populate establishment info
  - Added `updateLogoPreview()` to show logo preview when URL is entered
  - Added `handleFormSubmit()` to save establishment info and logo
  - Added `updateEstablishmentLogo()` to update logo separately

- **Client View (client-establishments.html)**:
  - Updated establishment card rendering to display logo at the top (max 150px height)
  - Logo displayed above establishment name and details

## Security Features

All image update endpoints validate that:
1. The resource (professional/service) belongs to the specified establishment
2. Only the establishment that owns the resource can update its image
3. Returns 403 Forbidden if validation fails

## Image Storage
- Current implementation uses URL-based images
- Images are expected to be hosted externally
- URL validation is performed on the client side (HTML5 URL input type)
- Image URLs are stored in the database (VARCHAR(500))

## Usage Guide

### For Establishment Owners

#### Adding a Professional with Image
1. Navigate to "Profissionais" page
2. Click "Novo Profissional"
3. Fill in the required fields (Nome, Email, Telefone, Especialidades)
4. Enter the image URL in "Foto do Profissional (URL)"
5. Click "Salvar Profissional"

#### Editing a Professional
1. Navigate to "Profissionais" page
2. Click "Editar" button on the professional card
3. Update the desired fields (including image URL)
4. Click "Salvar Profissional"

#### Adding a Service with Image
1. Navigate to "Serviços" page
2. Click "Novo Serviço"
3. Fill in the required fields
4. Enter the image URL in "Imagem do Serviço (URL)"
5. Click "Salvar Serviço"

#### Editing a Service
1. Navigate to "Serviços" page
2. Click the "Editar" button (pencil icon) on the service row
3. Update the desired fields (including image URL)
4. Click "Salvar Serviço"

#### Updating Establishment Logo
1. Navigate to "Administração" page
2. Scroll to "Informações do Estabelecimento" section
3. Enter the logo URL in "Logo do Estabelecimento (URL)"
4. A preview will appear below the input
5. Click "Salvar Informações"

### For Clients

- **View Professionals**: Professional images are automatically displayed on the "Profissionais" page
- **View Establishments**: Establishment logos are displayed on the "Estabelecimentos" page
- **Booking**: Professional images are visible during the booking process

## API Endpoints Summary

### Professionals
```
PUT /api/establishment/professionals/{id}/image?establishmentId={establishmentId}
Body: { "imageUrl": "string" }
```

### Services
```
PUT /api/establishment/services/{id}/image?establishmentId={establishmentId}
Body: { "imageUrl": "string" }
```

### Establishments
```
PUT /api/establishment/profile/{id}/image
Body: { "imageUrl": "string" }
```

## Database Schema
All entities already had `image_url VARCHAR(500)` column:
- `professionals.image_url`
- `services.image_url`
- `establishments.image_url`

No database migration required.

## Testing

### Manual Testing Checklist
- [ ] Create professional without image
- [ ] Create professional with image URL
- [ ] Edit professional and add image
- [ ] Edit professional and update image
- [ ] Verify professional image displays in establishment view
- [ ] Verify professional image displays in client view
- [ ] Create service without image
- [ ] Create service with image URL
- [ ] Edit service and add image
- [ ] Edit service and update image
- [ ] Update establishment logo
- [ ] Verify establishment logo displays in client establishments list
- [ ] Verify image preview works in establishment admin
- [ ] Verify invalid URLs are handled gracefully
- [ ] Verify images with errors show placeholder/icon
- [ ] Test multi-establishment data isolation (professionals/services from one establishment shouldn't be editable by another)

### Automated Tests
All existing backend tests pass:
```bash
./gradlew test
BUILD SUCCESSFUL
```

## Future Enhancements

1. **Direct File Upload**: Implement file upload instead of URL-only
   - Add file storage service (S3, Azure Blob, or local filesystem)
   - Add image upload endpoint with multipart/form-data
   - Add client-side file selection UI

2. **Image Validation**: Server-side validation of image URLs
   - Verify URL is accessible
   - Check image dimensions
   - Validate file size

3. **Image Processing**: Automatic image optimization
   - Resize images to standard dimensions
   - Compress images to reduce bandwidth
   - Generate thumbnails

4. **Placeholder Images**: Default images for different entity types
   - Professional avatars
   - Service icons
   - Establishment logos

## Notes
- All image URLs should be publicly accessible
- HTTPS URLs are recommended for security
- Image aspect ratios should be square for best display (1:1)
- Recommended image sizes:
  - Professional photos: 200x200px minimum
  - Service images: 300x300px minimum
  - Establishment logos: 300x300px minimum
