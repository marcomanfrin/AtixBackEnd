# Documentazione API - Atix Backend

**Versione:** 1.0
**Base URL:** `https://your-domain.com/api` (da configurare)

---

## Indice

1. [Autenticazione](#autenticazione)
2. [Enumerazioni](#enumerazioni)
3. [Endpoint API](#endpoint-api)
   - [Autenticazione](#endpoint-autenticazione)
   - [Utenti](#endpoint-utenti)
   - [Lavori](#endpoint-lavori)
   - [Clienti](#endpoint-clienti)
   - [Impianti](#endpoint-impianti)
   - [Ticket](#endpoint-ticket)
   - [Report Lavori](#endpoint-report-lavori)
   - [Riferimenti Cantiere](#endpoint-riferimenti-cantiere)
   - [Allegati](#endpoint-allegati)
4. [Modelli Dati](#modelli-dati)

---

## Autenticazione

L'API utilizza **JWT (JSON Web Token)** per l'autenticazione.

### Come autenticarsi

1. Effettuare una richiesta POST a `/auth/login` con credenziali valide
2. Ricevere un token JWT nella risposta
3. Includere il token in tutte le richieste successive nell'header:
   ```
   Authorization: Bearer {token}
   ```

### Ruoli Utente

- **USER**: Utente base
- **ADMIN**: Amministratore con permessi estesi
- **OWNER**: Proprietario con accesso completo

### Tipi Utente

- **ADMINISTRATION**: Utente amministrativo
- **TECHNICIAN**: Tecnico
- **SELLER**: Venditore

---

## Enumerazioni

### UserRole
```
USER, ADMIN, OWNER
```

### UserType
```
ADMINISTRATION, TECHNICIAN, SELLER
```

### TicketStatus
```
OPEN, IN_PROGRESS, RESOLVED, CLOSED
```

### ClientType
```
ATIX, FINAL
```

### AttachmentTargetType
```
WORK, PLANT, TICKET, REPORT
```

### WorksiteReferenceRole
```
PLUMBER, ELECTRICIAN
```

---

## Endpoint API

<a name="endpoint-autenticazione"></a>
### 🔐 Autenticazione

#### POST `/auth/login`
Effettua il login e ottiene un token JWT.

**Autenticazione richiesta:** ❌ No

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "user@example.com",
  "firstName": "Mario",
  "lastName": "Rossi",
  "role": "ADMIN"
}
```

**Errori:**
- `400 Bad Request`: Credenziali non valide
- `401 Unauthorized`: Email o password errati

---

<a name="endpoint-utenti"></a>
### 👤 Utenti

#### POST `/users`
Registra un nuovo utente.

**Autenticazione richiesta:** ✅ Sì (ADMIN o OWNER)

**Request Body:**
```json
{
  "firstName": "Mario",
  "lastName": "Rossi",
  "email": "mario.rossi@example.com",
  "password": "password123",
  "role": "USER",
  "type": "TECHNICIAN"
}
```

**Validazioni:**
- `firstName`: 2-50 caratteri, obbligatorio
- `lastName`: 2-50 caratteri, obbligatorio
- `email`: formato email valido, obbligatorio
- `password`: 8-100 caratteri, obbligatorio
- `role`: enum UserRole, obbligatorio
- `type`: enum UserType, obbligatorio

**Response 201:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "firstName": "Mario",
  "lastName": "Rossi",
  "email": "mario.rossi@example.com",
  "profileImageUrl": null,
  "role": "USER",
  "type": "TECHNICIAN"
}
```

---

#### GET `/users`
Ottiene la lista di tutti gli utenti.

**Autenticazione richiesta:** ✅ Sì

**Response 200:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "firstName": "Mario",
    "lastName": "Rossi",
    "email": "mario.rossi@example.com",
    "profileImageUrl": "https://ui-avatars.com/api/?name=Mario+Rossi",
    "role": "USER",
    "type": "TECHNICIAN"
  }
]
```

---

#### GET `/users/type/{type}`
Ottiene gli utenti filtrati per tipo.

**Autenticazione richiesta:** ✅ Sì

**Parametri URL:**
- `type`: UserType (ADMINISTRATION, TECHNICIAN, SELLER)

**Response 200:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "firstName": "Mario",
    "lastName": "Rossi",
    "email": "mario.rossi@example.com",
    "profileImageUrl": "https://ui-avatars.com/api/?name=Mario+Rossi",
    "role": "USER",
    "type": "TECHNICIAN"
  }
]
```

---

#### GET `/users/{id}`
Ottiene i dettagli di un utente specifico.

**Autenticazione richiesta:** ✅ Sì (OWNER o stesso utente)

**Parametri URL:**
- `id`: UUID dell'utente

**Response 200:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "firstName": "Mario",
  "lastName": "Rossi",
  "email": "mario.rossi@example.com",
  "profileImageUrl": "https://cloudinary.com/image.jpg",
  "role": "USER",
  "type": "TECHNICIAN"
}
```

---

#### PATCH `/users/{id}`
Aggiorna i dati di un utente.

**Autenticazione richiesta:** ✅ Sì (OWNER o stesso utente)

**Parametri URL:**
- `id`: UUID dell'utente

**Request Body:**
```json
{
  "firstName": "Mario",
  "lastName": "Rossi",
  "email": "mario.rossi@example.com"
}
```

**Nota:** Tutti i campi sono opzionali.

**Response 200:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "firstName": "Mario",
  "lastName": "Rossi",
  "email": "mario.rossi@example.com",
  "profileImageUrl": "https://cloudinary.com/image.jpg",
  "role": "USER",
  "type": "TECHNICIAN"
}
```

---

#### DELETE `/users/{id}`
Elimina un utente.

**Autenticazione richiesta:** ✅ Sì (OWNER)

**Parametri URL:**
- `id`: UUID dell'utente

**Response 204:** No Content

---

#### PATCH `/users/{id}/avatar`
Carica l'immagine del profilo utente.

**Autenticazione richiesta:** ✅ Sì (stesso utente)

**Content-Type:** `multipart/form-data`

**Parametri URL:**
- `id`: UUID dell'utente

**Form Data:**
- `avatar`: File immagine

**Response 200:**
```json
{
  "imageUrl": "https://cloudinary.com/image.jpg"
}
```

---

#### PATCH `/users/{id}/password`
Aggiorna la password dell'utente.

**Autenticazione richiesta:** ✅ Sì (stesso utente)

**Parametri URL:**
- `id`: UUID dell'utente

**Request Body:**
```json
{
  "currentPassword": "oldPassword123",
  "newPassword": "newPassword456"
}
```

**Response 204:** No Content

---

<a name="endpoint-lavori"></a>
### 🔨 Lavori (Works)

#### POST `/works`
Crea un nuovo lavoro.

**Autenticazione richiesta:** ✅ Sì

**Request Body:**
```json
{
  "name": "Impianto Elettrico Ufficio A",
  "bidNumber": "BID-2024-001",
  "sellerId": "550e8400-e29b-41d4-a716-446655440000",
  "orderNumber": "ORD-2024-001",
  "orderDate": "2024-01-15",
  "electricalSchemaProgression": 0,
  "programmingProgression": 0,
  "expectedStartDate": "2024-02-01",
  "plantId": "660e8400-e29b-41d4-a716-446655440000",
  "atixClientId": "770e8400-e29b-41d4-a716-446655440000",
  "finalClientId": "880e8400-e29b-41d4-a716-446655440000",
  "nasSubDirectory": "/projects/office-a",
  "expectedOfficeHours": 40,
  "expectedPlantHours": 80,
  "ticketId": "990e8400-e29b-41d4-a716-446655440000"
}
```

**Campi obbligatori:**
- `name`: 2-100 caratteri
- `bidNumber`: stringa
- `orderNumber`: stringa
- `orderDate`: data (formato ISO: YYYY-MM-DD)
- `atixClientId`: UUID
- `nasSubDirectory`: stringa

**Campi opzionali:**
- `sellerId`: UUID
- `electricalSchemaProgression`: 0-100
- `programmingProgression`: 0-100
- `expectedStartDate`: data
- `plantId`: UUID
- `finalClientId`: UUID
- `expectedOfficeHours`: numero
- `expectedPlantHours`: numero
- `ticketId`: UUID

**Response 201:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Impianto Elettrico Ufficio A",
  "description": "Descrizione dettagliata del lavoro",
  "bidNumber": "BID-2024-001",
  "seller": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "firstName": "Mario",
    "lastName": "Rossi",
    "email": "mario.rossi@example.com",
    "profileImageUrl": "https://ui-avatars.com/api/?name=Mario+Rossi",
    "role": "USER",
    "type": "SELLER"
  },
  "orderNumber": "ORD-2024-001",
  "orderDate": "2024-01-15",
  "electricalSchemaProgression": 0,
  "programmingProgression": 0,
  "expectedStartDate": "2024-02-01",
  "completed": false,
  "completedAt": null,
  "createdAt": "2024-01-15T10:30:00",
  "invoiced": false,
  "invoicedAt": null,
  "plant": {
    "id": "660e8400-e29b-41d4-a716-446655440000",
    "name": "Impianto Centrale"
  },
  "atixClient": {
    "id": "770e8400-e29b-41d4-a716-446655440000",
    "name": "Atix Client Name",
    "type": "ATIX"
  },
  "finalClient": {
    "id": "880e8400-e29b-41d4-a716-446655440000",
    "name": "Final Client Name",
    "type": "FINAL"
  },
  "worksiteReferenceAssignments": [],
  "assignedTechnicians": [],
  "nasSubDirectory": "/projects/office-a",
  "expectedOfficeHours": 40,
  "expectedPlantHours": 80,
  "ticket": null
}
```

---

#### GET `/works`
Ottiene la lista dei lavori con paginazione e filtri opzionali.

**Autenticazione richiesta:** ✅ Sì

**Query Parameters (tutti opzionali):**
- `clientId`: UUID - Filtra per client (atix o final)
- `atixClientId`: UUID - Filtra per client Atix
- `finalClientId`: UUID - Filtra per client finale
- `sellerId`: UUID - Filtra per venditore
- `plantId`: UUID - Filtra per impianto
- `ticketId`: UUID - Filtra per ticket
- `technicianId`: UUID - Filtra per tecnico assegnato
- `completed`: boolean - Filtra per stato completamento
- `invoiced`: boolean - Filtra per stato fatturazione
- `orderDateFrom`: date (YYYY-MM-DD) - Data ordine da
- `orderDateTo`: date (YYYY-MM-DD) - Data ordine a
- `expectedStartDateFrom`: date (YYYY-MM-DD) - Data inizio prevista da
- `expectedStartDateTo`: date (YYYY-MM-DD) - Data inizio prevista a
- `name`: string - Cerca per nome
- `bidNumber`: string - Cerca per numero offerta
- `orderNumber`: string - Cerca per numero ordine
- `page`: numero (default: 0) - Numero pagina
- `size`: numero (default: 20) - Elementi per pagina
- `sort`: string - Campo ordinamento (es: "name,asc" o "orderDate,desc")

**Response 200:**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Impianto Elettrico Ufficio A",
      "bidNumber": "BID-2024-001",
      "orderNumber": "ORD-2024-001",
      "orderDate": "2024-01-15",
      "completed": false,
      "invoiced": false,
      "electricalSchemaProgression": 25,
      "programmingProgression": 10,
      "nasSubDirectory": "/projects/office-a",
      "relatedPlantNasDirectory": "/plants/centrale",
      "expectedStartDate": "2024-02-01",
      "plant": {
        "id": "660e8400-e29b-41d4-a716-446655440000",
        "name": "Impianto Centrale"
      },
      "finalClient": {
        "id": "880e8400-e29b-41d4-a716-446655440000",
        "name": "Final Client Name",
        "type": "FINAL"
      },
      "assignedTechnicians": [
        {
          "id": "990e8400-e29b-41d4-a716-446655440000",
          "technicianId": "550e8400-e29b-41d4-a716-446655440000",
          "technicianFirstName": "Mario",
          "technicianLastName": "Rossi",
          "technicianEmail": "mario.rossi@example.com",
          "assignedAt": "2024-01-15T10:30:00"
        }
      ]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "size": 20,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 1,
  "first": true,
  "empty": false
}
```

---

#### GET `/works/{id}`
Ottiene i dettagli completi di un lavoro.

**Autenticazione richiesta:** ✅ Sì

**Parametri URL:**
- `id`: UUID del lavoro

**Response 200:** Stesso formato del POST `/works`

---

#### PATCH `/works/{id}`
Aggiorna un lavoro esistente.

**Autenticazione richiesta:** ✅ Sì (ADMIN o OWNER)

**Parametri URL:**
- `id`: UUID del lavoro

**Request Body:** Tutti i campi sono opzionali
```json
{
  "name": "Nuovo Nome",
  "electricalSchemaProgression": 50,
  "programmingProgression": 30
}
```

**Response 200:** Dettagli completi del lavoro aggiornato

---

#### PATCH `/works/{id}/close`
Chiude un lavoro (marca come completato).

**Autenticazione richiesta:** ✅ Sì (TECHNICIAN)

**Parametri URL:**
- `id`: UUID del lavoro

**Response 204:** No Content

---

#### PATCH `/works/{id}/invoice`
Marca un lavoro come fatturato.

**Autenticazione richiesta:** ✅ Sì (ADMINISTRATION)

**Parametri URL:**
- `id`: UUID del lavoro

**Response 204:** No Content

---

#### POST `/works/{id}/assign-technician`
Assegna un tecnico a un lavoro.

**Autenticazione richiesta:** ✅ Sì (ADMIN, OWNER o TECHNICIAN)

**Parametri URL:**
- `id`: UUID del lavoro

**Request Body:**
```json
{
  "technicianId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response 201:** Created

---

#### POST `/works/{id}/add-reference`
Aggiunge un riferimento cantiere al lavoro.

**Autenticazione richiesta:** ✅ Sì

**Parametri URL:**
- `id`: UUID del lavoro

**Request Body:**
```json
{
  "worksiteReferenceId": "550e8400-e29b-41d4-a716-446655440000",
  "role": "ELECTRICIAN"
}
```

**Response 201:** Created

---

<a name="endpoint-clienti"></a>
### 🏢 Clienti

#### POST `/clients`
Crea un nuovo cliente.

**Autenticazione richiesta:** ✅ Sì (SELLER)

**Request Body:**
```json
{
  "name": "ABC Corporation",
  "type": "ATIX"
}
```

**Validazioni:**
- `name`: 2-100 caratteri, obbligatorio
- `type`: enum ClientType (ATIX o FINAL), obbligatorio

**Response 201:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "ABC Corporation",
  "type": "ATIX"
}
```

---

#### GET `/clients`
Ottiene la lista dei clienti con paginazione.

**Autenticazione richiesta:** ✅ Sì

**Query Parameters:**
- `page`: numero (default: 0)
- `size`: numero (default: 20)
- `sort`: string (es: "name,asc")

**Response 200:**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "ABC Corporation",
      "type": "ATIX"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

---

#### GET `/clients/{id}`
Ottiene i dettagli di un cliente.

**Autenticazione richiesta:** ✅ Sì

**Parametri URL:**
- `id`: UUID del cliente

**Response 200:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "ABC Corporation",
  "type": "ATIX"
}
```

---

#### PATCH `/clients/{id}`
Aggiorna un cliente.

**Autenticazione richiesta:** ✅ Sì

**Parametri URL:**
- `id`: UUID del cliente

**Request Body:** Campi opzionali
```json
{
  "name": "ABC Corporation Updated",
  "type": "FINAL"
}
```

**Response 200:** Dettagli del cliente aggiornato

---

#### DELETE `/clients/{id}`
Elimina un cliente.

**Autenticazione richiesta:** ✅ Sì (OWNER)

**Parametri URL:**
- `id`: UUID del cliente

**Response 204:** No Content

---

<a name="endpoint-impianti"></a>
### 🏭 Impianti (Plants)

#### POST `/plants`
Crea un nuovo impianto.

**Autenticazione richiesta:** ✅ Sì (ADMIN o OWNER)

**Request Body:**
```json
{
  "name": "Impianto Centrale",
  "notes": "Impianto principale con sistema di automazione",
  "nasDirectory": "/plants/centrale",
  "pswPhrase": "frase-sicurezza",
  "pswPlatform": "password-piattaforma",
  "pswStation": "password-stazione"
}
```

**Validazioni:** Tutti i campi sono obbligatori
- `name`: 2-100 caratteri

**Response 201:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Impianto Centrale",
  "notes": "Impianto principale con sistema di automazione",
  "nasDirectory": "/plants/centrale",
  "pswPhrase": "frase-sicurezza",
  "pswPlatform": "password-piattaforma",
  "pswStation": "password-stazione"
}
```

---

#### GET `/plants`
Ottiene la lista degli impianti con paginazione.

**Autenticazione richiesta:** ✅ Sì

**Query Parameters:**
- `page`: numero (default: 0)
- `size`: numero (default: 20)
- `sort`: string

**Response 200:** Formato paginato standard

---

#### GET `/plants/{id}`
Ottiene i dettagli di un impianto.

**Autenticazione richiesta:** ✅ Sì

**Parametri URL:**
- `id`: UUID dell'impianto

**Response 200:** Dettagli completi dell'impianto

---

#### PATCH `/plants/{id}`
Aggiorna un impianto.

**Autenticazione richiesta:** ✅ Sì

**Parametri URL:**
- `id`: UUID dell'impianto

**Request Body:** Campi opzionali

**Response 200:** Dettagli dell'impianto aggiornato

---

#### DELETE `/plants/{id}`
Elimina un impianto.

**Autenticazione richiesta:** ✅ Sì (OWNER)

**Parametri URL:**
- `id`: UUID dell'impianto

**Response 204:** No Content

---

<a name="endpoint-ticket"></a>
### 🎫 Ticket

#### POST `/tickets`
Crea un nuovo ticket.

**Autenticazione richiesta:** ✅ Sì (ADMINISTRATION)

**Request Body:**
```json
{
  "senderEmail": "customer@example.com",
  "orderNumberId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Problema con impianto",
  "description": "Descrizione dettagliata del problema",
  "status": "OPEN"
}
```

**Validazioni:**
- `name`: 2-100 caratteri, obbligatorio
- `description`: obbligatorio
- `status`: enum TicketStatus, obbligatorio
- `senderEmail`: formato email valido (opzionale)
- `orderNumberId`: UUID (opzionale)

**Response 201:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "senderEmail": "customer@example.com",
  "orderNumberId": "660e8400-e29b-41d4-a716-446655440000",
  "name": "Problema con impianto",
  "description": "Descrizione dettagliata del problema",
  "status": "OPEN",
  "createdAt": "2024-01-15T10:30:00"
}
```

---

#### GET `/tickets`
Ottiene la lista dei ticket con filtri opzionali.

**Autenticazione richiesta:** ✅ Sì

**Query Parameters (tutti opzionali):**
- `senderEmail`: string - Filtra per email mittente
- `orderNumberId`: UUID - Filtra per lavoro associato
- `name`: string - Cerca per nome
- `description`: string - Cerca nella descrizione
- `status`: TicketStatus - Filtra per stato
- `createdAtFrom`: datetime (ISO 8601) - Data creazione da
- `createdAtTo`: datetime (ISO 8601) - Data creazione a
- `page`: numero (default: 0)
- `size`: numero (default: 20)
- `sort`: string

**Response 200:** Formato paginato standard

---

#### GET `/tickets/{id}`
Ottiene i dettagli di un ticket.

**Autenticazione richiesta:** ✅ Sì

**Parametri URL:**
- `id`: UUID del ticket

**Response 200:** Dettagli completi del ticket

---

#### PATCH `/tickets/{id}`
Aggiorna un ticket.

**Autenticazione richiesta:** ✅ Sì

**Parametri URL:**
- `id`: UUID del ticket

**Request Body:** Campi opzionali

**Response 200:** Dettagli del ticket aggiornato

---

#### DELETE `/tickets/{id}`
Elimina un ticket.

**Autenticazione richiesta:** ✅ Sì (OWNER)

**Parametri URL:**
- `id`: UUID del ticket

**Response 204:** No Content

---

<a name="endpoint-report-lavori"></a>
### 📊 Report Lavori

#### GET `/work-reports/work/{workId}`
Ottiene il report di un lavoro specifico.

**Autenticazione richiesta:** ✅ Sì

**Parametri URL:**
- `workId`: UUID del lavoro

**Response 200:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "workId": "660e8400-e29b-41d4-a716-446655440000",
  "totalHours": 45.50,
  "createdAt": "2024-01-15T10:30:00",
  "entries": [
    {
      "id": "770e8400-e29b-41d4-a716-446655440000",
      "description": "Installazione quadro elettrico",
      "hours": 8.00
    }
  ]
}
```

---

#### POST `/work-reports/entries`
Crea una nuova voce nel report di lavoro.

**Autenticazione richiesta:** ✅ Sì (TECHNICIAN)

**Request Body:**
```json
{
  "workId": "550e8400-e29b-41d4-a716-446655440000",
  "description": "Installazione quadro elettrico",
  "hours": 8.0
}
```

**Response 201:**
```json
{
  "id": "770e8400-e29b-41d4-a716-446655440000",
  "description": "Installazione quadro elettrico",
  "hours": 8.0
}
```

---

#### PATCH `/work-reports/entries/{id}`
Aggiorna una voce del report.

**Autenticazione richiesta:** ✅ Sì (TECHNICIAN)

**Parametri URL:**
- `id`: UUID della voce

**Request Body:** Campi opzionali
```json
{
  "description": "Descrizione aggiornata",
  "hours": 10.5
}
```

**Response 200:** Dettagli della voce aggiornata

---

#### GET `/work-reports/entries/work/{workId}`
Ottiene tutte le voci del report per un lavoro.

**Autenticazione richiesta:** ✅ Sì

**Parametri URL:**
- `workId`: UUID del lavoro

**Response 200:**
```json
[
  {
    "id": "770e8400-e29b-41d4-a716-446655440000",
    "description": "Installazione quadro elettrico",
    "hours": 8.0
  }
]
```

---

#### DELETE `/work-reports/entries/{id}`
Elimina una voce del report.

**Autenticazione richiesta:** ✅ Sì (OWNER)

**Parametri URL:**
- `id`: UUID della voce

**Response 204:** No Content

---

<a name="endpoint-riferimenti-cantiere"></a>
### 🏗️ Riferimenti Cantiere

#### POST `/worksite-references`
Crea un nuovo riferimento cantiere.

**Autenticazione richiesta:** ✅ Sì

**Request Body:**
```json
{
  "name": "Idraulico Rossi Mario"
}
```

**Response 201:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Idraulico Rossi Mario"
}
```

---

#### GET `/worksite-references`
Ottiene la lista di tutti i riferimenti cantiere.

**Autenticazione richiesta:** ✅ Sì

**Response 200:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Idraulico Rossi Mario"
  }
]
```

---

#### GET `/worksite-references/{id}`
Ottiene i dettagli di un riferimento cantiere.

**Autenticazione richiesta:** ✅ Sì

**Parametri URL:**
- `id`: UUID del riferimento

**Response 200:** Dettagli completi

---

#### PATCH `/worksite-references/{id}`
Aggiorna un riferimento cantiere.

**Autenticazione richiesta:** ✅ Sì

**Parametri URL:**
- `id`: UUID del riferimento

**Request Body:**
```json
{
  "name": "Nuovo nome"
}
```

**Response 200:** Dettagli aggiornati

---

#### DELETE `/worksite-references/{id}`
Elimina un riferimento cantiere.

**Autenticazione richiesta:** ✅ Sì (OWNER)

**Parametri URL:**
- `id`: UUID del riferimento

**Response 204:** No Content

---

<a name="endpoint-allegati"></a>
### 📎 Allegati

#### POST `/attachments/{targetType}/{targetId}`
Carica e collega un allegato a un'entità.

**Autenticazione richiesta:** ✅ Sì

**Content-Type:** `multipart/form-data`

**Parametri URL:**
- `targetType`: AttachmentTargetType (WORK, PLANT, TICKET, REPORT)
- `targetId`: UUID dell'entità target

**Form Data:**
- `file`: File da caricare

**Response 201:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "attachment": {
    "id": "660e8400-e29b-41d4-a716-446655440000",
    "url": "https://res.cloudinary.com/...",
    "publicId": "cloudinary_public_id",
    "resourceType": "image",
    "type": "IMAGE",
    "uploadedAt": "2024-01-15T10:30:00"
  },
  "targetType": "WORK",
  "targetId": "770e8400-e29b-41d4-a716-446655440000"
}
```

---

#### GET `/attachments/{targetType}/{targetId}`
Ottiene tutti gli allegati di un'entità.

**Autenticazione richiesta:** ✅ Sì

**Parametri URL:**
- `targetType`: AttachmentTargetType
- `targetId`: UUID dell'entità

**Response 200:**
```json
[
  {
    "id": "660e8400-e29b-41d4-a716-446655440000",
    "url": "https://res.cloudinary.com/...",
    "publicId": "cloudinary_public_id",
    "resourceType": "image",
    "type": "IMAGE",
    "uploadedAt": "2024-01-15T10:30:00"
  }
]
```

---

#### DELETE `/attachments/{attachmentId}`
Elimina un allegato.

**Autenticazione richiesta:** ✅ Sì

**Parametri URL:**
- `attachmentId`: UUID dell'allegato

**Response 204:** No Content

---

## Modelli Dati

### User
```typescript
{
  id: string (UUID)
  firstName: string
  lastName: string
  email: string
  profileImageUrl?: string
  role: "USER" | "ADMIN" | "OWNER"
  type: "ADMINISTRATION" | "TECHNICIAN" | "SELLER"
}
```

### Work (Dettaglio)
```typescript
{
  id: string (UUID)
  name: string
  description?: string
  bidNumber: string
  seller?: UserSummary
  orderNumber: string
  orderDate: string (ISO date)
  electricalSchemaProgression: number (0-100)
  programmingProgression: number (0-100)
  expectedStartDate?: string (ISO date)
  completed: boolean
  completedAt?: string (ISO datetime)
  createdAt: string (ISO datetime)
  invoiced: boolean
  invoicedAt?: string (ISO datetime)
  plant?: Plant
  atixClient: Client
  finalClient?: Client
  worksiteReferenceAssignments: WorksiteReferenceAssignment[]
  assignedTechnicians: WorkAssignment[]
  nasSubDirectory: string
  expectedOfficeHours?: number
  expectedPlantHours?: number
  ticket?: Ticket
}
```

### Work (Sommario)
```typescript
{
  id: string (UUID)
  name: string
  bidNumber: string
  orderNumber: string
  orderDate: string (ISO date)
  completed: boolean
  invoiced: boolean
  electricalSchemaProgression: number (0-100)
  programmingProgression: number (0-100)
  nasSubDirectory: string
  relatedPlantNasDirectory?: string
  expectedStartDate?: string (ISO date)
  plant?: Plant
  finalClient?: Client
  assignedTechnicians: WorkAssignment[]
}
```

### Client
```typescript
{
  id: string (UUID)
  name: string
  type: "ATIX" | "FINAL"
}
```

### Plant
```typescript
{
  id: string (UUID)
  name: string
  notes: string
  nasDirectory: string
  pswPhrase: string
  pswPlatform: string
  pswStation: string
}
```

### Ticket
```typescript
{
  id: string (UUID)
  senderEmail?: string
  orderNumberId?: string (UUID)
  name: string
  description: string
  status: "OPEN" | "IN_PROGRESS" | "RESOLVED" | "CLOSED"
  createdAt: string (ISO datetime)
}
```

### WorkReport
```typescript
{
  id: string (UUID)
  workId: string (UUID)
  totalHours: number (decimal)
  createdAt: string (ISO datetime)
  entries: WorkReportEntry[]
}
```

### WorkReportEntry
```typescript
{
  id: string (UUID)
  description: string
  hours: number (decimal)
}
```

### WorksiteReference
```typescript
{
  id: string (UUID)
  name: string
}
```

### WorkAssignment
```typescript
{
  id: string (UUID)
  technicianId: string (UUID)
  technicianFirstName: string
  technicianLastName: string
  technicianEmail: string
  assignedAt: string (ISO datetime)
}
```

### Attachment
```typescript
{
  id: string (UUID)
  url: string
  publicId: string
  resourceType: string
  type: string
  uploadedAt: string (ISO datetime)
}
```

---

## Codici di Stato HTTP

- **200 OK**: Richiesta completata con successo
- **201 Created**: Risorsa creata con successo
- **204 No Content**: Operazione completata senza contenuto di risposta
- **400 Bad Request**: Richiesta non valida (errori di validazione)
- **401 Unauthorized**: Non autenticato
- **403 Forbidden**: Non autorizzato (ruolo insufficiente)
- **404 Not Found**: Risorsa non trovata
- **500 Internal Server Error**: Errore del server

---

## Formato Errori

Tutte le risposte di errore seguono questo formato:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/users"
}
```

Per errori di validazione multipli:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "email",
      "message": "Email must be valid"
    },
    {
      "field": "password",
      "message": "Password must be between 8 and 100 characters"
    }
  ],
  "path": "/api/users"
}
```

---

## Paginazione

Gli endpoint che supportano la paginazione accettano i seguenti query parameters:

- `page`: Numero della pagina (inizia da 0)
- `size`: Numero di elementi per pagina (default: 20)
- `sort`: Campo di ordinamento con direzione (es: `name,asc` o `createdAt,desc`)

Esempio di risposta paginata:

```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {...},
    "offset": 0
  },
  "totalElements": 100,
  "totalPages": 5,
  "last": false,
  "first": true,
  "size": 20,
  "number": 0,
  "numberOfElements": 20,
  "empty": false
}
```

---

## Note per gli Sviluppatori Frontend

### CORS
L'API supporta CORS. Assicurarsi che il dominio frontend sia configurato nelle whitelist del backend.

### Date e Orari
- Le date sono in formato ISO 8601: `YYYY-MM-DD`
- I datetime sono in formato ISO 8601: `YYYY-MM-DDTHH:mm:ss`
- Tutti i datetime sono in UTC

### Upload di File
Gli upload di file utilizzano `multipart/form-data`. Il campo del file deve essere chiamato come specificato nella documentazione (`avatar`, `file`, etc.).

### Validazione
I messaggi di errore di validazione sono in italiano e provengono direttamente dai constraint delle entità.

### Token JWT
- Il token JWT ha una scadenza configurabile (default: 24 ore)
- Includere sempre il token nell'header `Authorization: Bearer {token}`
- Gestire il caso di token scaduto (401) reimplementando il login

---

**Ultima modifica:** 2026-01-03
**Versione API:** 1.0
