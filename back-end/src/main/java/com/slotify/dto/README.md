# DTOs (Data Transfer Objects)

This package contains Data Transfer Objects used for API communication between the frontend and backend.

## Purpose

DTOs serve several important purposes:

1. **Type Safety**: Provide strongly-typed objects for request/response data
2. **Validation**: Enable request validation using Jakarta Bean Validation annotations
3. **Documentation**: Make API contracts clearer and self-documenting
4. **Security**: Prevent over-posting by controlling which fields are exposed
5. **Versioning**: Allow API evolution without changing domain models

## Structure

### Request DTOs
- `ClientLoginRequest.java` - Client authentication request
- `ClientRegisterRequest.java` - Client registration request
- `EstablishmentLoginRequest.java` - Establishment authentication request
- `AppointmentCreateRequest.java` - Appointment creation request

### Response DTOs
- `ClientResponse.java` - Client data response
- `AppointmentResponse.java` - Appointment data response
- `ApiResponse.java` - Generic API response wrapper

## Usage Example

Instead of using `Map<String, Object>` in controllers:

```java
// Before (not recommended)
@PostMapping("/login")
public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
    // ...
}

// After (recommended)
@PostMapping("/login")
public ResponseEntity<ApiResponse<ClientResponse>> login(@RequestBody ClientLoginRequest request) {
    // ...
}
```

## Best Practices

1. **Validation**: Add appropriate validation annotations to request DTOs
2. **Documentation**: Use JavaDoc comments to document fields and purpose
3. **Naming**: Use clear, descriptive names ending with "Request" or "Response"
4. **Immutability**: Consider making DTOs immutable where appropriate
5. **Mapping**: Use mapping utilities to convert between DTOs and entities