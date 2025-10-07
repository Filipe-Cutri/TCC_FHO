# Frontend Migration Guide - Multi-Establishment Isolation

## Overview

This guide helps frontend developers update their code to work with the new multi-establishment data isolation security features.

## What Changed?

All endpoints that operate on individual resources (appointments, professionals, services) now require an `establishmentId` query parameter to ensure proper data isolation.

## Step-by-Step Migration

### Step 1: Store Establishment ID on Login

When a user logs in, store their `establishmentId` for use in subsequent requests.

**Before:**
```javascript
// Login endpoint
fetch('/api/establishment/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
})
.then(response => response.json())
.then(data => {
  if (data.success) {
    // Store user info
    localStorage.setItem('userId', data.user.id);
    localStorage.setItem('userName', data.user.name);
  }
});
```

**After:**
```javascript
// Login endpoint
fetch('/api/establishment/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
})
.then(response => response.json())
.then(data => {
  if (data.success) {
    // Store user info including establishmentId
    localStorage.setItem('userId', data.user.id);
    localStorage.setItem('userName', data.user.name);
    localStorage.setItem('establishmentId', data.user.establishmentId); // NEW
  }
});
```

### Step 2: Create Helper Function

Create a utility function to get the establishment ID:

```javascript
// utils/auth.js
export function getEstablishmentId() {
  const establishmentId = localStorage.getItem('establishmentId');
  if (!establishmentId) {
    console.error('No establishment ID found. User may not be logged in.');
    window.location.href = '/login';
    return null;
  }
  return establishmentId;
}
```

### Step 3: Update GET Requests

Add `establishmentId` parameter to all GET requests for individual resources.

#### Appointments

**Before:**
```javascript
// Get appointment by ID
fetch(`/api/establishment/appointments/${appointmentId}`)
  .then(response => response.json())
  .then(data => {
    if (data.success) {
      console.log('Appointment:', data.data);
    }
  });
```

**After:**
```javascript
import { getEstablishmentId } from './utils/auth.js';

// Get appointment by ID
const establishmentId = getEstablishmentId();
fetch(`/api/establishment/appointments/${appointmentId}?establishmentId=${establishmentId}`)
  .then(response => response.json())
  .then(data => {
    if (data.success) {
      console.log('Appointment:', data.data);
    } else if (response.status === 404) {
      console.log('Appointment not found or access denied');
    }
  });
```

#### Professionals

**Before:**
```javascript
fetch(`/api/establishment/professionals/${professionalId}`)
```

**After:**
```javascript
const establishmentId = getEstablishmentId();
fetch(`/api/establishment/professionals/${professionalId}?establishmentId=${establishmentId}`)
```

#### Services

**Before:**
```javascript
fetch(`/api/establishment/services/${serviceId}`)
```

**After:**
```javascript
const establishmentId = getEstablishmentId();
fetch(`/api/establishment/services/${serviceId}?establishmentId=${establishmentId}`)
```

### Step 4: Update PUT/DELETE Requests

Add `establishmentId` parameter to all update and delete operations.

#### Update Appointment Status

**Before:**
```javascript
fetch(`/api/establishment/appointments/${id}/status`, {
  method: 'PUT',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ status: 'confirmed' })
})
```

**After:**
```javascript
const establishmentId = getEstablishmentId();
fetch(`/api/establishment/appointments/${id}/status?establishmentId=${establishmentId}`, {
  method: 'PUT',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ status: 'confirmed' })
})
```

#### Update Professional

**Before:**
```javascript
fetch(`/api/establishment/professionals/${id}`, {
  method: 'PUT',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    name: 'Updated Name',
    email: 'updated@email.com',
    phone: '1234567890',
    specialties: 'Haircut, Beard'
  })
})
```

**After:**
```javascript
const establishmentId = getEstablishmentId();
fetch(`/api/establishment/professionals/${id}?establishmentId=${establishmentId}`, {
  method: 'PUT',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    name: 'Updated Name',
    email: 'updated@email.com',
    phone: '1234567890',
    specialties: 'Haircut, Beard'
  })
})
```

#### Delete Service

**Before:**
```javascript
fetch(`/api/establishment/services/${id}`, {
  method: 'DELETE'
})
```

**After:**
```javascript
const establishmentId = getEstablishmentId();
fetch(`/api/establishment/services/${id}?establishmentId=${establishmentId}`, {
  method: 'DELETE'
})
```

### Step 5: Handle 403 Forbidden Responses

Add proper error handling for unauthorized access attempts.

```javascript
const establishmentId = getEstablishmentId();
fetch(`/api/establishment/appointments/${id}?establishmentId=${establishmentId}`)
  .then(response => {
    if (response.status === 403) {
      // Access denied
      alert('Acesso negado: você não tem permissão para acessar este recurso');
      return null;
    }
    if (response.status === 404) {
      // Not found or not in your establishment
      alert('Recurso não encontrado');
      return null;
    }
    return response.json();
  })
  .then(data => {
    if (data && data.success) {
      console.log('Data:', data.data);
    }
  })
  .catch(error => {
    console.error('Error:', error);
    alert('Erro ao carregar dados');
  });
```

## Complete Examples

### Example 1: Appointment Management Component

```javascript
// components/AppointmentDetail.js
import { getEstablishmentId } from '../utils/auth.js';

class AppointmentDetail {
  constructor(appointmentId) {
    this.appointmentId = appointmentId;
    this.establishmentId = getEstablishmentId();
  }

  async load() {
    try {
      const response = await fetch(
        `/api/establishment/appointments/${this.appointmentId}?establishmentId=${this.establishmentId}`
      );
      
      if (response.status === 403) {
        throw new Error('Acesso negado');
      }
      
      if (response.status === 404) {
        throw new Error('Agendamento não encontrado');
      }
      
      const data = await response.json();
      if (data.success) {
        this.renderAppointment(data.data);
      }
    } catch (error) {
      this.showError(error.message);
    }
  }

  async updateStatus(newStatus) {
    try {
      const response = await fetch(
        `/api/establishment/appointments/${this.appointmentId}/status?establishmentId=${this.establishmentId}`,
        {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ status: newStatus })
        }
      );
      
      if (response.status === 403) {
        throw new Error('Você não tem permissão para modificar este agendamento');
      }
      
      const data = await response.json();
      if (data.success) {
        this.showSuccess('Status atualizado com sucesso');
        this.load(); // Reload to show updated data
      }
    } catch (error) {
      this.showError(error.message);
    }
  }

  renderAppointment(appointment) {
    // Render appointment details
  }

  showError(message) {
    // Show error to user
  }

  showSuccess(message) {
    // Show success to user
  }
}
```

### Example 2: Professional Management

```javascript
// components/ProfessionalManager.js
import { getEstablishmentId } from '../utils/auth.js';

async function updateProfessional(professionalId, formData) {
  const establishmentId = getEstablishmentId();
  
  try {
    const response = await fetch(
      `/api/establishment/professionals/${professionalId}?establishmentId=${establishmentId}`,
      {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      }
    );
    
    if (!response.ok) {
      if (response.status === 403) {
        throw new Error('Acesso negado: profissional não pertence ao seu estabelecimento');
      }
      throw new Error('Erro ao atualizar profissional');
    }
    
    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Update failed:', error);
    throw error;
  }
}

async function deleteProfessional(professionalId) {
  const establishmentId = getEstablishmentId();
  
  if (!confirm('Tem certeza que deseja remover este profissional?')) {
    return;
  }
  
  try {
    const response = await fetch(
      `/api/establishment/professionals/${professionalId}?establishmentId=${establishmentId}`,
      {
        method: 'DELETE'
      }
    );
    
    if (!response.ok) {
      if (response.status === 403) {
        throw new Error('Acesso negado');
      }
      throw new Error('Erro ao remover profissional');
    }
    
    const data = await response.json();
    return data;
  } catch (error) {
    console.error('Delete failed:', error);
    throw error;
  }
}
```

### Example 3: Service Management

```javascript
// components/ServiceManager.js
import { getEstablishmentId } from '../utils/auth.js';

class ServiceManager {
  static async getService(serviceId) {
    const establishmentId = getEstablishmentId();
    const response = await fetch(
      `/api/establishment/services/${serviceId}?establishmentId=${establishmentId}`
    );
    
    if (response.status === 404) {
      return null;
    }
    
    if (response.status === 403) {
      throw new Error('Acesso negado');
    }
    
    const data = await response.json();
    return data.success ? data.data : null;
  }

  static async updateServiceStatus(serviceId, status) {
    const establishmentId = getEstablishmentId();
    const response = await fetch(
      `/api/establishment/services/${serviceId}/status?establishmentId=${establishmentId}`,
      {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status })
      }
    );
    
    if (!response.ok) {
      const data = await response.json();
      throw new Error(data.message || 'Erro ao atualizar status');
    }
    
    return await response.json();
  }
}
```

## Testing Your Migration

### 1. Test with Valid Establishment ID

```javascript
// Should work fine
const validEstablishmentId = '1'; // Your establishment
localStorage.setItem('establishmentId', validEstablishmentId);

fetch(`/api/establishment/appointments/123?establishmentId=${validEstablishmentId}`)
  .then(r => r.json())
  .then(console.log); // Should return appointment if it exists
```

### 2. Test with Invalid Establishment ID

```javascript
// Should return 404 or 403
const invalidEstablishmentId = '999'; // Different establishment
fetch(`/api/establishment/appointments/123?establishmentId=${invalidEstablishmentId}`)
  .then(r => {
    console.log('Status:', r.status); // Should be 404 (not found in your context)
    return r.json();
  })
  .then(console.log);
```

### 3. Test Missing Establishment ID

```javascript
// Should fail with proper error
fetch(`/api/establishment/appointments/123`) // Missing establishmentId
  .then(r => {
    console.log('Status:', r.status); // May get 400 or validation error
    return r.json();
  })
  .then(console.log);
```

## Checklist

Use this checklist to ensure complete migration:

- [ ] Updated login handler to store `establishmentId`
- [ ] Created `getEstablishmentId()` helper function
- [ ] Updated all appointment GET requests to include `establishmentId`
- [ ] Updated all appointment PUT requests to include `establishmentId`
- [ ] Updated all professional GET requests to include `establishmentId`
- [ ] Updated all professional PUT requests to include `establishmentId`
- [ ] Updated all professional DELETE requests to include `establishmentId`
- [ ] Updated all service GET requests to include `establishmentId`
- [ ] Updated all service PUT requests to include `establishmentId`
- [ ] Updated all service DELETE requests to include `establishmentId`
- [ ] Added error handling for 403 Forbidden responses
- [ ] Added error handling for 404 Not Found responses
- [ ] Tested with valid establishment ID
- [ ] Tested with invalid establishment ID
- [ ] Updated user documentation

## Common Issues & Solutions

### Issue 1: "No establishment ID found"

**Symptom**: Helper function redirects to login
**Solution**: Ensure `establishmentId` is stored on login

```javascript
// Check if it's being stored
console.log('Stored ID:', localStorage.getItem('establishmentId'));
```

### Issue 2: Always getting 404

**Symptom**: All requests return 404
**Solution**: Verify you're using the correct establishment ID

```javascript
// Check what ID you're using
const estId = getEstablishmentId();
console.log('Using establishment ID:', estId);
```

### Issue 3: 403 on valid requests

**Symptom**: Getting forbidden on resources that should be accessible
**Solution**: Verify the resource actually belongs to your establishment

```javascript
// Check in database or via list endpoint
fetch(`/api/establishment/appointments?establishmentId=${estId}`)
  .then(r => r.json())
  .then(data => {
    console.log('All appointments:', data);
    // Verify the ID you're trying to access is in this list
  });
```

## Need Help?

If you encounter issues during migration:

1. Check the browser console for detailed error messages
2. Verify the `establishmentId` is correctly stored and retrieved
3. Review the API documentation in `docs/MULTI_ESTABLISHMENT_ISOLATION.md`
4. Check that you're using the latest backend version

## Summary

✅ Add `establishmentId` to all individual resource endpoints
✅ Store establishment ID on login
✅ Handle 403/404 responses appropriately
✅ Test thoroughly with valid and invalid IDs
