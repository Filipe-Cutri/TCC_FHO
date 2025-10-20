# Quick Reference: Client-Establishment Integration

## For Developers

### How to Get Client's Selected Establishment

**JavaScript (Frontend):**
```javascript
// Method 1: Using ClientSessionManager
const establishmentId = window.clientSession.getSelectedEstablishmentId();

// Method 2: Direct session access
const session = window.clientSession.getSession();
const establishmentId = session ? session.selectedEstablishmentId : null;

// Method 3: From sessionStorage (fallback)
const establishmentId = sessionStorage.getItem('selectedEstablishmentId');
```

**Java (Backend):**
```java
// From Client entity
Optional<Client> client = clientService.findById(clientId);
Long establishmentId = client.get().getSelectedEstablishmentId();
```

### How to Update Client's Establishment Selection

**Frontend:**
```javascript
// Update via API and sync session
const response = await window.apiClient.put('/api/client/establishment', {
    clientId: clientId,
    establishmentId: establishmentId
});

if (response.success) {
    window.clientSession.setSelectedEstablishmentId(establishmentId);
}
```

**Backend:**
```java
// Using ClientService
Client client = clientService.updateSelectedEstablishment(clientId, establishmentId);
```

### API Endpoints Reference

#### Client-Establishment Management
```
PUT /api/client/establishment
Body: { clientId: Long, establishmentId: Long }
Response: { success: true, client: {...} }
```

#### List Establishments (for selection)
```
GET /api/client/establishments
Response: { success: true, data: [...], count: N }
```

#### Get Establishment Details
```
GET /api/client/establishments/{id}
Response: { success: true, data: {...} }
```

#### Get Establishment's Services
```
GET /api/client/establishments/{id}/services
Response: { success: true, data: [...], count: N }
```

#### Get Establishment's Professionals
```
GET /api/client/establishments/{id}/professionals
Response: { success: true, data: [...], count: N }
```

## For Designers/Frontend Developers

### Where Establishment Information Appears

1. **Dashboard** (`client-dashboard.html`)
   - Banner showing selected establishment
   - Warning if no establishment selected
   - Link to change establishment

2. **Services Page** (`client-services.html`)
   - Services filtered by establishment
   - Prompt to select establishment if none chosen

3. **Professionals Page** (`client-professionals.html`)
   - Professionals filtered by establishment
   - Redirect to establishments page if none selected

4. **Booking Flow**
   - Pre-populated with establishment context
   - All data from selected establishment only

### UI Components

**Establishment Banner (Dashboard):**
```html
<div class="alert alert-info">
    <div class="d-flex align-items-center justify-content-between">
        <div>
            <i class="fas fa-store me-2"></i>
            <strong>Estabelecimento Selecionado:</strong> {name}
            <span class="badge bg-primary ms-2">{category}</span>
        </div>
        <a href="client-establishments.html" class="btn btn-sm btn-outline-primary">
            Trocar
        </a>
    </div>
</div>
```

**No Establishment Warning:**
```html
<div class="alert alert-warning">
    <div class="d-flex align-items-center justify-content-between">
        <div>
            <i class="fas fa-exclamation-triangle me-2"></i>
            <strong>Atenção:</strong> Selecione um estabelecimento...
        </div>
        <a href="client-establishments.html" class="btn btn-sm btn-primary">
            Selecionar
        </a>
    </div>
</div>
```

## For QA/Testers

### Test Scenarios

#### Scenario 1: New Client Registration
1. Navigate to client registration
2. Fill in required fields
3. Select an establishment (optional)
4. Register
5. **Verify:** Dashboard shows selected establishment OR prompt to select

#### Scenario 2: Client Login
1. Login with existing client
2. **Verify:** If client has establishment, banner appears on dashboard
3. **Verify:** Services page shows only that establishment's services
4. **Verify:** Professionals page shows only that establishment's professionals

#### Scenario 3: Change Establishment
1. Login as client
2. Go to Establishments page
3. Select different establishment
4. **Verify:** Dashboard updates to show new establishment
5. **Verify:** Services and professionals change accordingly

#### Scenario 4: Book Appointment
1. Login as client with establishment
2. Navigate to Services
3. Select a service
4. Select a professional
5. Complete booking
6. **Verify:** Appointment created with correct establishment ID

#### Scenario 5: Data Isolation
1. Login as Client A (linked to Establishment 1)
2. Note the services and professionals shown
3. Logout and login as Client B (linked to Establishment 2)
4. **Verify:** Different services and professionals appear
5. **Verify:** No data from Establishment 1 visible to Client B

### Expected Behaviors

✅ **Correct:**
- Client sees only data from their selected establishment
- Dashboard shows establishment banner
- Booking creates appointments with correct establishment
- Establishment can be changed by client
- Sessions persist across page navigation

❌ **Incorrect (Bugs):**
- Client sees services from all establishments
- No establishment shown on dashboard
- Appointment created with wrong establishment
- Session lost on page refresh
- Cannot change establishment

## For Database Administrators

### Key Database Fields

**clients table:**
```sql
selected_establishment_id BIGINT
  - Foreign key to establishments(id)
  - Can be NULL
  - Updated when client selects establishment
```

**Migration Script:**
```sql
-- Add column (already applied)
ALTER TABLE clients 
ADD COLUMN selected_establishment_id BIGINT;

-- Add foreign key
ALTER TABLE clients
ADD CONSTRAINT fk_client_selected_establishment 
    FOREIGN KEY (selected_establishment_id) 
    REFERENCES establishments(id) 
    ON DELETE SET NULL;

-- Add index for performance
CREATE INDEX idx_clients_selected_establishment_id 
ON clients(selected_establishment_id);
```

### Useful Queries

**Find clients without establishment:**
```sql
SELECT id, name, email 
FROM clients 
WHERE selected_establishment_id IS NULL 
  AND active = true;
```

**Count clients per establishment:**
```sql
SELECT e.name, COUNT(c.id) as client_count
FROM establishments e
LEFT JOIN clients c ON c.selected_establishment_id = e.id
GROUP BY e.id, e.name
ORDER BY client_count DESC;
```

**Verify appointment-establishment consistency:**
```sql
SELECT 
    a.id as appointment_id,
    c.selected_establishment_id as client_establishment,
    a.establishment_id as appointment_establishment,
    CASE 
        WHEN c.selected_establishment_id = a.establishment_id THEN 'OK'
        ELSE 'MISMATCH'
    END as status
FROM appointments a
JOIN clients c ON a.client_id = c.id
WHERE c.selected_establishment_id IS NOT NULL;
```

## Troubleshooting

### Issue: Client doesn't see any services
**Solution:** Check if client has selected an establishment
```javascript
// In browser console
console.log(window.clientSession.getSelectedEstablishmentId());
```

### Issue: Services from all establishments visible
**Solution:** Verify API is using establishment filter
```javascript
// Check API call in Network tab
// Should include: /api/client/establishments/{id}/services
```

### Issue: Session lost on page refresh
**Solution:** Check localStorage and sessionStorage
```javascript
// In browser console
console.log(localStorage.getItem('slotfy_client_session'));
console.log(sessionStorage.getItem('selectedEstablishmentId'));
```

### Issue: Appointment created with wrong establishment
**Solution:** Verify booking flow passes correct establishment ID
```javascript
// Check the booking request payload
// Should include: establishmentId: <correct_id>
```

## Common Pitfalls

1. ❌ **Don't** read from `localStorage.getItem('user')` - use `window.clientSession.getSession()`
2. ❌ **Don't** hardcode establishment IDs - always get from session
3. ❌ **Don't** skip establishment validation in booking flow
4. ✅ **Do** use ClientSessionManager for all session operations
5. ✅ **Do** show visual feedback about selected establishment
6. ✅ **Do** handle cases where no establishment is selected

## Additional Resources

- Full documentation: `CLIENT_ESTABLISHMENT_INTEGRATION_FIX.md`
- Database schema: `database_schema.sql`
- Migration: `database_migration_add_establishment_to_client.sql`
- API docs: `api_endpoints.json`
