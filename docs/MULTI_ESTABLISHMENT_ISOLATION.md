# Multi-Establishment Data Isolation - Implementation Documentation

## Overview

This document describes the implementation of multi-establishment data isolation in the Slotfy scheduling system, ensuring that each establishment's data remains completely isolated from other establishments.

## Security Model

The system implements **logical isolation** using the `establishment_id` field as a security boundary:

- All critical tables (appointments, professionals, services, etc.) contain an `establishment_id` foreign key
- All operations must validate that the resource belongs to the authenticated user's establishment
- Cross-establishment data access is strictly prohibited and results in `SecurityException` (HTTP 403)

## Implementation Details

### Service Layer Validation

Added validation methods to enforce establishment isolation at the service layer:

#### AppointmentService

```java
/**
 * Validate that an appointment belongs to the specified establishment
 * This is critical for multi-establishment data isolation
 */
public void validateAppointmentBelongsToEstablishment(Long appointmentId, Long establishmentId) {
    // Throws SecurityException if appointment doesn't belong to establishment
}

/**
 * Get appointment by ID with establishment validation
 * Ensures multi-establishment data isolation
 */
public Optional<Appointment> findByIdAndEstablishment(Long appointmentId, Long establishmentId) {
    // Returns empty if appointment doesn't belong to establishment
}
```

Similar methods added for:
- `updateStatus(id, status, establishmentId)`
- `reschedule(id, newDateTime, establishmentId)`
- `updateNotes(id, notes, establishmentId)`

#### ProfessionalService

```java
public void validateProfessionalBelongsToEstablishment(Long professionalId, Long establishmentId)
public Optional<Professional> findByIdAndEstablishment(Long professionalId, Long establishmentId)
public Professional updateProfessional(..., Long establishmentId)
public Professional updateStatus(Long id, ProfessionalStatus status, Long establishmentId)
public void deleteProfessional(Long id, Long establishmentId)
```

#### ServiceService

```java
public void validateServiceBelongsToEstablishment(Long serviceId, Long establishmentId)
public Optional<Service> findByIdAndEstablishment(Long serviceId, Long establishmentId)
public Service updateService(..., Long establishmentId)
public Service updateStatus(Long id, ServiceStatus status, Long establishmentId)
public void deleteService(Long id, Long establishmentId)
```

### Controller Layer Updates

Updated all controllers to require `establishmentId` parameter for operations on individual resources:

#### AppointmentController

**GET Operations:**
```java
@GetMapping("/{id}")
public ResponseEntity<Map<String, Object>> getAppointment(
    @PathVariable Long id,
    @RequestParam Long establishmentId)
```

**UPDATE Operations:**
```java
@PutMapping("/{id}/status")
public ResponseEntity<Map<String, Object>> updateStatus(
    @PathVariable Long id,
    @RequestBody Map<String, String> request,
    @RequestParam Long establishmentId)
```

All endpoints now return HTTP 403 (Forbidden) when attempting to access resources from a different establishment.

#### ProfessionalController & ServiceController

Similar updates applied to:
- `GET /{id}` - requires establishmentId parameter
- `PUT /{id}` - validates establishment ownership
- `PUT /{id}/status` - validates establishment ownership
- `DELETE /{id}` - validates establishment ownership

### Error Handling

The system uses specific exception types for security violations:

- **`SecurityException`**: Thrown when attempting to access resources from a different establishment
- **HTTP 403 Forbidden**: Returned to clients when `SecurityException` is caught
- **Message**: "Acesso negado: [resource] não pertence ao estabelecimento"

### Example API Calls

**Valid Request (Same Establishment):**
```bash
GET /api/establishment/appointments/123?establishmentId=1
# Returns appointment if it belongs to establishment 1
```

**Invalid Request (Different Establishment):**
```bash
GET /api/establishment/appointments/123?establishmentId=2
# Returns 404 Not Found (appointment doesn't exist in this context)
```

**Invalid Update Attempt:**
```bash
PUT /api/establishment/appointments/123/status?establishmentId=2
# Returns 403 Forbidden with message "Acesso negado: agendamento não pertence ao estabelecimento"
```

## Testing

Comprehensive test suite created in `EstablishmentIsolationTest.java` covering:

### Appointment Isolation Tests
- ✅ Should NOT find appointment from different establishment
- ✅ Should find appointment from same establishment
- ✅ Should throw SecurityException when updating from different establishment
- ✅ Should successfully update from same establishment
- ✅ Reschedule validation
- ✅ Notes update validation

### Professional Isolation Tests
- ✅ GET by ID validation
- ✅ UPDATE validation
- ✅ UPDATE status validation
- ✅ DELETE validation

### Service Isolation Tests
- ✅ GET by ID validation
- ✅ UPDATE validation
- ✅ UPDATE status validation
- ✅ DELETE validation

### Cross-Establishment Validation
- ✅ Batch validation preventing cross-establishment access

## Security Guarantees

1. **No Direct Database Access**: Controllers cannot directly access repositories; they must go through service layer
2. **Mandatory Validation**: All service methods validate establishment ownership before operations
3. **Fail-Safe Design**: GET operations return empty/not found rather than exposing existence of resources
4. **Comprehensive Coverage**: All CRUD operations (Create, Read, Update, Delete) are protected

## Breaking Changes

⚠️ **API Changes**: The following endpoints now require `establishmentId` parameter:

### AppointmentController
- `GET /api/establishment/appointments/{id}?establishmentId={id}`
- `PUT /api/establishment/appointments/{id}/status?establishmentId={id}`
- `PUT /api/establishment/appointments/{id}/reschedule?establishmentId={id}`
- `PUT /api/establishment/appointments/{id}/notes?establishmentId={id}`
- `PUT /api/establishment/appointments/{id}/cancel?establishmentId={id}`
- `PUT /api/establishment/appointments/{id}/confirm?establishmentId={id}`
- `PUT /api/establishment/appointments/{id}/complete?establishmentId={id}`

### ProfessionalController
- `GET /api/establishment/professionals/{id}?establishmentId={id}`
- `PUT /api/establishment/professionals/{id}?establishmentId={id}`
- `PUT /api/establishment/professionals/{id}/status?establishmentId={id}`
- `DELETE /api/establishment/professionals/{id}?establishmentId={id}`

### ServiceController
- `GET /api/establishment/services/{id}?establishmentId={id}`
- `PUT /api/establishment/services/{id}?establishmentId={id}`
- `PUT /api/establishment/services/{id}/status?establishmentId={id}`
- `DELETE /api/establishment/services/{id}?establishmentId={id}`

## Frontend Integration

Frontend applications must:

1. Store the authenticated user's `establishmentId` (obtained from login response)
2. Include `establishmentId` as query parameter in all requests to protected endpoints
3. Handle HTTP 403 responses appropriately (e.g., show access denied message)

Example:
```javascript
// Store establishment ID from login
const establishmentId = loginResponse.user.establishmentId;

// Include in all requests
fetch(`/api/establishment/appointments/123?establishmentId=${establishmentId}`)
```

## Future Enhancements

1. **JWT Token Integration**: Include `establishmentId` in JWT claims to prevent tampering
2. **Aspect-Oriented Security**: Use AOP to automatically validate establishment context
3. **Audit Logging**: Log all cross-establishment access attempts for security monitoring
4. **Row-Level Security**: Consider database-level security policies for defense in depth

## Compliance

This implementation ensures compliance with:
- **Multi-tenancy requirements**: Complete data isolation between tenants
- **LGPD/GDPR**: Data segregation for privacy compliance
- **SaaS best practices**: Secure multi-tenant architecture

## References

- Database Schema: `database_schema.sql`
- Model Documentation: `docs/database/TABELAS_BANCO_DADOS.md`
- Architecture: `ARCHITECTURE.md`
- Problem Statement: Issue prompt describing multi-establishment isolation requirements
