# Password Recovery Feature - Slotfy

## Overview

This document describes the password recovery feature implemented for both client and establishment users in the Slotfy system.

## Features

- **Secure Token Generation**: Uses cryptographically secure random tokens (32 bytes)
- **Token Hashing**: Tokens are hashed using SHA-256 with UTF-8 encoding before storage
- **Token Expiration**: Reset tokens expire after 1 hour
- **Email Delivery**: Password reset links are sent via SendGrid email service
- **Email Enumeration Prevention**: The system doesn't reveal whether an email exists in the database
- **Separate Flows**: Different endpoints for client and establishment users

## Database Schema

The password recovery feature requires two additional columns in both the `clients` and `establishment_users` tables:

```sql
-- For clients table
ALTER TABLE clients 
ADD COLUMN reset_password_token_hash VARCHAR(255),
ADD COLUMN reset_password_expiry BIGINT;

-- For establishment_users table
ALTER TABLE establishment_users 
ADD COLUMN reset_password_token_hash VARCHAR(255),
ADD COLUMN reset_password_expiry BIGINT;
```

The migration script is available at: `database_migration_password_reset.sql`

## API Endpoints

### Client Password Recovery

#### Request Password Reset
```
POST /api/client/forgot-password
Content-Type: application/json

{
  "email": "client@example.com"
}
```

**Response** (always returns success to prevent email enumeration):
```json
{
  "success": true,
  "message": "Se o email existir em nosso sistema, você receberá instruções para redefinir sua senha"
}
```

#### Reset Password
```
POST /api/client/reset-password
Content-Type: application/json

{
  "token": "abc123...",
  "newPassword": "newSecurePassword123"
}
```

**Success Response**:
```json
{
  "success": true,
  "message": "Senha redefinida com sucesso"
}
```

**Error Response**:
```json
{
  "success": false,
  "message": "Token de redefinição inválido" | "Token de redefinição expirado" | "Senha deve ter pelo menos 6 caracteres"
}
```

### Establishment Password Recovery

The establishment endpoints work identically to the client endpoints, but use different paths:

- **Request Reset**: `POST /api/establishment/forgot-password`
- **Reset Password**: `POST /api/establishment/reset-password`

## Frontend Pages

### Forgot Password Pages

1. **Client Forgot Password**: `/pages/client/client-forgot-password.html`
2. **Establishment Forgot Password**: `/pages/establishment/establishment-forgot-password.html`

Both pages:
- Accept an email address
- Call the appropriate API endpoint
- Display success message on submission
- Link back to login page

### Reset Password Page

- **Location**: `/pages/reset-password.html`
- **Parameters**: 
  - `token`: The reset token from the email link
  - `type`: Either "client" or "establishment" (optional, defaults to "client")
- **Features**:
  - Password confirmation field
  - Password validation (minimum 6 characters)
  - Automatic redirect to appropriate login page on success

## Email Template

The password reset email includes:

- Professional branded header
- Clear call-to-action button
- Fallback link for copy/paste
- Expiration warning (1 hour)
- Security notice for unsolicited emails

## Configuration

### Environment Variables

The following environment variables must be configured:

```bash
# SendGrid Email Service
SENDGRID_API_KEY=SG.your-api-key-here
SENDGRID_FROM=noreply@slotfy.com

# Frontend URL for password reset links
FRONTEND_URL=https://your-frontend-domain.com
```

### Application Properties

The configuration is in `application.properties`:

```properties
# SendGrid Email Configuration
sendgrid.api.key=${SENDGRID_API_KEY:}
sendgrid.from.email=${SENDGRID_FROM:noreply@slotfy.com}
sendgrid.from.name=Slotfy - Sistema de Agendamento

# Frontend URL for password reset links
frontend.url=${FRONTEND_URL:http://localhost:3000}
```

## Security Considerations

1. **Token Security**:
   - Tokens are 32 bytes of cryptographically secure random data
   - Tokens are hashed (SHA-256) before database storage
   - Plain-text tokens are never stored
   - Tokens are single-use (cleared after successful reset)

2. **Token Expiration**:
   - All tokens expire after 1 hour
   - Expired tokens are rejected with appropriate error message
   - Expired tokens are cleared from the database on next reset attempt

3. **Email Enumeration Prevention**:
   - The forgot-password endpoint always returns success
   - This prevents attackers from discovering valid email addresses
   - The actual email sending happens asynchronously

4. **Password Validation**:
   - Minimum 6 characters required
   - Same validation as registration

5. **Rate Limiting** (Recommended):
   - Consider implementing rate limiting on forgot-password endpoints
   - Prevents abuse and email spam
   - Not currently implemented but recommended for production

## Testing

### Manual Testing Steps

#### Client Password Reset

1. Navigate to client login page
2. Click "Esqueci a senha"
3. Enter a registered email address
4. Check email inbox for reset link
5. Click the link in the email
6. Enter new password (twice)
7. Submit the form
8. Verify redirect to login page
9. Login with new password

#### Establishment Password Reset

Same steps as client, but using the establishment login flow.

### Test Data

For local testing, you can manually insert test tokens:

```sql
-- For testing, insert a known hashed token
-- Token: test-token-123 (this is just an example, generate real tokens in production)
UPDATE clients 
SET reset_password_token_hash = 'hashed_value_here',
    reset_password_expiry = (EXTRACT(EPOCH FROM NOW()) + 3600) * 1000
WHERE email = 'test@example.com';
```

## Troubleshooting

### Email Not Received

1. Check SendGrid API key is configured correctly
2. Verify email is not in spam folder
3. Check SendGrid dashboard for delivery status
4. Verify SENDGRID_FROM email is verified in SendGrid

### Token Expired Error

- Reset tokens expire after 1 hour
- Request a new password reset if token has expired
- Check server time is synchronized correctly

### Invalid Token Error

- Token may have been tampered with
- Token may have already been used
- Request a new password reset

## Future Enhancements

- [ ] Add rate limiting to prevent abuse
- [ ] Add email templates for different languages
- [ ] Add SMS-based password reset as alternative
- [ ] Add password strength meter on reset page
- [ ] Add audit logging for password reset attempts
- [ ] Add configurable token expiration time
- [ ] Add password reset history

## Support

For issues or questions about the password recovery feature:

1. Check this documentation
2. Review the code in:
   - `PasswordResetService.java`
   - `ClientService.java`
   - `EstablishmentUserService.java`
   - `ClientAuthController.java`
   - `EstablishmentAuthController.java`
3. Contact the development team
