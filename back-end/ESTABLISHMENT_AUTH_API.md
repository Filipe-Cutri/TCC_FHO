# Establishment Authentication API Documentation

## Endpoints Created

### 1. POST /api/establishment/register
**Description**: Register a new establishment admin account

**Request Body**:
```json
{
  "name": "João Silva",
  "email": "joao@restaurant.com",
  "password": "password123",
  "establishmentName": "Restaurante do João", // Optional
  "establishmentId": "12345" // Optional, auto-generated if not provided
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Conta criada com sucesso",
  "user": {
    "id": 1,
    "name": "João Silva",
    "email": "joao@restaurant.com",
    "role": "admin",
    "roleDescription": "Administrador",
    "establishmentId": 12345
  }
}
```

**Response Error (400)**:
```json
{
  "success": false,
  "message": "Nome, email e senha são obrigatórios"
}
```

### 2. POST /api/establishment/login
**Description**: Login for establishment users (admin/staff)

**Request Body**:
```json
{
  "email": "joao@restaurant.com",
  "password": "password123",
  "role": "admin" // Optional role verification
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Login realizado com sucesso",
  "user": {
    "id": 1,
    "name": "João Silva",
    "email": "joao@restaurant.com",
    "role": "admin",
    "roleDescription": "Administrador",
    "establishmentId": 12345
  }
}
```

### 3. POST /api/establishment/forgot-password
**Description**: Request password reset for establishment users

**Request Body**:
```json
{
  "email": "joao@restaurant.com"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Instruções enviadas para o e-mail"
}
```

### 4. POST /api/reset-password
**Description**: Reset password using token from email

**Request Body**:
```json
{
  "token": "uuid-token-from-email",
  "newPassword": "newpassword123"
}
```

**Response Success (200)**:
```json
{
  "success": true,
  "message": "Senha alterada com sucesso"
}
```

### 5. POST /api/establishment/create-staff
**Description**: Create new staff user (admin only)

**Request Body**:
```json
{
  "name": "Maria Silva",
  "email": "maria@restaurant.com",
  "password": "password123",
  "establishmentId": "12345"
}
```

### 6. GET /api/establishment/roles
**Description**: Get available user roles

**Response**:
```json
{
  "success": true,
  "roles": {
    "admin": {
      "code": "admin",
      "description": "Administrador"
    },
    "staff": {
      "code": "staff",
      "description": "Funcionário"
    }
  }
}
```

## Security Features Implemented

1. **Password Hashing**: All passwords are encrypted using BCrypt
2. **Input Validation**: Required fields validation and password length check
3. **Email Uniqueness**: Prevents duplicate email registrations
4. **Token-based Password Reset**: Secure password reset with time-limited tokens
5. **Role-based Authentication**: Support for admin and staff roles

## Database Integration

- All user data is persisted to database via JPA/Hibernate
- EstablishmentUser entity with proper constraints
- Repository pattern for data access
- BaseEntity with automatic timestamp management

## Error Handling

- Comprehensive error responses with meaningful messages
- Input validation with appropriate HTTP status codes
- Exception handling for database and internal errors