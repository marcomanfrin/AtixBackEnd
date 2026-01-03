# AtixBackEnd

A comprehensive backend application for managing technical work operations, built with Spring Boot and PostgreSQL. This system handles user management, client relations, work assignments, ticketing, and reporting with role-based access control and real-time integrations.

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Features](#features)
- [Database Schema](#database-schema)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Environment Variables](#environment-variables)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Authentication & Authorization](#authentication--authorization)
- [GraphQL API](#graphql-api)
- [Project Structure](#project-structure)

---

## Overview

AtixBackEnd is an enterprise-grade backend system designed to manage technical operations for service companies. The application provides a complete solution for:

- **User Management**: Multi-role user system with three employee types (Technicians, Administrative, Sellers)
- **Client & Plant Management**: Track clients and their industrial plants
- **Work Order System**: Create, assign, and track technical work orders
- **Ticketing System**: Manage support tickets linked to work orders
- **Work Reports**: Detailed reporting system with time tracking entries
- **Document Management**: Upload and organize attachments (photos, PDFs, documents)
- **Dashboard Analytics**: Real-time statistics and summaries via REST and GraphQL

---

## Technology Stack

### Core Framework
- **Spring Boot**: 4.0.0
- **Java**: 21
- **Maven**: Build automation and dependency management

### Backend Technologies
- **Spring Data JPA**: Database persistence and ORM (Hibernate)
- **Spring Security**: Authentication and authorization
- **Spring Web MVC**: RESTful API development
- **Spring GraphQL**: GraphQL API endpoint
- **PostgreSQL**: Relational database

### Security & Authentication
- **JWT (JSON Web Tokens)**: Stateless authentication using JJWT library
- **BCrypt**: Password hashing (strength: 12)

### Third-Party Integrations
- **Cloudinary**: Cloud-based image and file storage
- **Mailgun**: Transactional email service

### Additional Libraries
- **Bean Validation**: Request validation with Jakarta Validation
- **Jackson**: JSON serialization/deserialization

---

## Features

### 1. User Management
- User registration with role assignment (ADMIN, OWNER, USER)
- Three employee types: Technician, Administrative, Seller
- User inheritance hierarchy using JPA SINGLE_TABLE strategy
- Profile image upload with Cloudinary
- Password update functionality
- Email notifications on registration

### 2. Client & Plant Management
- CRUD operations for clients (individual or company)
- Plant management linked to clients
- Pagination support for large datasets
- Filtering and search capabilities

### 3. Work Order System
- Create and manage work orders
- Link works to clients (both Atix client and final client)
- Assign plants to work orders
- Work order statuses (pending, in progress, completed)
- Invoice tracking
- Assign multiple technicians to work orders
- Add worksite references to work orders
- Advanced filtering (by client, seller, plant, technician, date ranges, completion status, etc.)

### 4. Ticketing System
- Create support tickets
- Link tickets to work orders
- Ticket statuses: OPEN, IN_PROGRESS, RESOLVED, CLOSED
- Filter by status, sender, date range, description
- Automatic ticket creation workflow

### 5. Work Report Management
- Automatic work report creation for each work order
- Add time-tracking entries with descriptions
- Update and delete report entries
- Technician-specific access controls

### 6. Document Management
- Upload attachments to various entities (works, plants, tickets, reports)
- Attachment types: PHOTO, PDF, DOC, OTHER
- Cloudinary integration for secure storage
- Automatic file organization by entity type
- Link attachments to multiple target types

### 7. Dashboard & Analytics
- Summary statistics (client count, plant count, work completion rates)
- Ticket status distribution
- Recent work previews
- Recent ticket previews
- Available via both REST and GraphQL APIs

### 8. Advanced Query Features
- Pagination with Spring Data Pageable
- Dynamic filtering using JPA Specifications
- Sorting capabilities
- Aggregation queries (counts, grouping)
- Date range queries
- Complex multi-table joins
- Polymorphic queries on inheritance hierarchy

### 9. Security Features
- JWT-based stateless authentication
- Role-based access control (RBAC)
- Method-level security with @PreAuthorize
- Custom security expressions (isSelf, isTechnician, isAdministrative)
- CORS configuration
- Password validation and hashing

### 10. Error Handling & Validation
- Global exception handler with @ControllerAdvice
- Structured error responses (ErrorDTO)
- Comprehensive bean validation on all DTOs
- Meaningful error messages for all exception types

---

## Database Schema

The application uses **15 entities** mapped to **14-15 database tables**:

### Core Entities
1. **User** (users) - Base entity with SINGLE_TABLE inheritance
   - TechnicianUser
   - AdministrativeUser
   - SellerUser
2. **Client** (clients) - Individual or company clients
3. **Plant** (plants) - Industrial plants owned by clients
4. **Work** (works) - Work orders
5. **WorkAssignment** (work_assignments) - Many-to-many: Work ↔ Technician
6. **Ticket** (tickets) - Support tickets
7. **WorkReport** (work_reports) - One-to-one with Work
8. **WorkReportEntry** (work_report_entries) - Time tracking entries
9. **WorksiteReference** (worksite_references) - Site contact persons
10. **WorksiteReferenceAssignment** (worksite_reference_assignments) - Many-to-many: Work ↔ Reference
11. **Attachment** (attachments) - File metadata
12. **AttachmentLink** (attachment_links) - Polymorphic attachment links

### Key Relationships
- User → WorkAssignment (one-to-many)
- Work → WorkAssignment (one-to-many)
- Client → Work (one-to-many, dual relationship: atixClient + finalClient)
- Plant → Work (one-to-many)
- Work ↔ WorkReport (one-to-one)
- Work ↔ Ticket (one-to-one)
- WorkReport → WorkReportEntry (one-to-many)

---

## Prerequisites

Before running this application, ensure you have:

- **Java 21** or higher installed
- **Maven 3.9+** installed
- **PostgreSQL 14+** running locally or remotely
- **Cloudinary Account** (free tier available at [cloudinary.com](https://cloudinary.com))
- **Mailgun Account** (free tier available at [mailgun.com](https://www.mailgun.com))

---

## Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/AtixBackEnd.git
cd AtixBackEnd
```

### 2. Create PostgreSQL Database

```sql
CREATE DATABASE atix_backend;
```

### 3. Configure Environment Variables

Create a file named `env.properties` in the project root directory:

```properties
# Database Configuration
PG_DB_NAME=atix_backend
PG_USERNAME=your_postgres_username
PG_PASSWORD=your_postgres_password

# Cloudinary Configuration
CLOUDINARY_NAME=your_cloudinary_cloud_name
CLOUDINARY_KEY=your_cloudinary_api_key
CLOUDINARY_SECRET=your_cloudinary_api_secret

# Mailgun Configuration
MAILGUN_DOMAIN=your_mailgun_domain
MAILGUN_API_KEY=your_mailgun_api_key
MAILGUN_SENDER=noreply@yourdomain.com

# JWT Configuration
JWT_SECRET=your_very_secure_jwt_secret_key_at_least_256_bits

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

**⚠️ IMPORTANT**: Never commit `env.properties` to version control. Add it to `.gitignore`.

### 4. Install Dependencies

```bash
./mvnw clean install
```

---

## Environment Variables

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `PG_DB_NAME` | PostgreSQL database name | `atix_backend` |
| `PG_USERNAME` | PostgreSQL username | `postgres` |
| `PG_PASSWORD` | PostgreSQL password | `yourpassword` |
| `CLOUDINARY_NAME` | Cloudinary cloud name | `your-cloud-name` |
| `CLOUDINARY_KEY` | Cloudinary API key | `123456789012345` |
| `CLOUDINARY_SECRET` | Cloudinary API secret | `abcdefghijklmnopqrstuvwxyz` |
| `MAILGUN_DOMAIN` | Mailgun domain | `mg.yourdomain.com` |
| `MAILGUN_API_KEY` | Mailgun API key | `key-xxxxxxxxxxxxxxxxxxxxxxxx` |
| `MAILGUN_SENDER` | Email sender address | `noreply@yourdomain.com` |
| `JWT_SECRET` | JWT signing secret (min 256 bits) | `your-super-secret-key-here` |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins (comma-separated) | `http://localhost:3000` |

### How to Get API Keys

**Cloudinary:**
1. Sign up at [cloudinary.com](https://cloudinary.com)
2. Go to Dashboard → Account Details
3. Copy Cloud Name, API Key, and API Secret

**Mailgun:**
1. Sign up at [mailgun.com](https://www.mailgun.com)
2. Verify your domain or use Mailgun sandbox domain
3. Go to API → API Keys
4. Copy your API key and domain

---

## Running the Application

### Development Mode

```bash
./mvnw spring-boot:run
```

### Production Build

```bash
./mvnw clean package
java -jar target/AtixBackEnd-0.0.1-SNAPSHOT.jar
```

### Verify Application Started

The application will start on:
- **Base URL**: `http://localhost:3001/api`
- **GraphQL Endpoint**: `http://localhost:3001/api/graphql`
- **GraphiQL UI**: `http://localhost:3001/api/graphiql` (development only)

You should see in the console:
```
Started AtixBackEndApplication in X.XXX seconds
```

### First Launch - Default Admin User

**IMPORTANT**: On the first launch, if the database is empty, the application will automatically create a default OWNER user with the following credentials:

- **Email**: `admin@atixbackend.com`
- **Password**: `Admin123!`
- **Role**: OWNER
- **Type**: ADMINISTRATION

You will see this message in the console logs:
```
Default OWNER user created successfully!
Email: admin@atixbackend.com
Password: Admin123!
IMPORTANT: Please change these credentials after first login!
```

**Security Warning**: For security reasons, you should:
1. Log in with these credentials immediately after first launch
2. Change the password using the `PATCH /users/{id}/password` endpoint
3. Optionally update the email address using the `PATCH /users/{id}` endpoint

This default user is only created if no users exist in the database. On subsequent launches, this step will be skipped.

---

## API Documentation

### Postman Collection

A complete Postman collection is included in the repository:

📁 **File**: `AtixBackEnd API.postman_collection.json`

**Import Instructions:**
1. Open Postman
2. Click "Import" button
3. Select the JSON file
4. The collection will be imported with all endpoints organized by feature

**Collection includes:**
- All REST endpoints with example requests
- Pre-configured environment variables
- Authentication headers
- Sample payloads for all POST/PATCH requests

### REST API Endpoints Overview

**Base URL**: `http://localhost:3001/api`

#### Authentication
- `POST /auth/login` - Login and receive JWT token

#### Users
- `POST /users` - Register new user (ADMIN, OWNER only)
- `GET /users` - Get all users
- `GET /users/type/{type}` - Get users by type (TECHNICIAN, ADMINISTRATION, SELLER)
- `GET /users/{id}` - Get user details
- `PATCH /users/{id}` - Update user
- `PATCH /users/{id}/avatar` - Upload profile image
- `PATCH /users/{id}/password` - Change password
- `DELETE /users/{id}` - Delete user (OWNER only)

#### Clients
- `POST /clients` - Create client
- `GET /clients` - Get all clients (paginated)
- `GET /clients/{id}` - Get client by ID
- `PATCH /clients/{id}` - Update client
- `DELETE /clients/{id}` - Delete client

#### Plants
- `POST /plants` - Create plant
- `GET /plants` - Get all plants (paginated)
- `GET /plants/{id}` - Get plant by ID
- `PATCH /plants/{id}` - Update plant
- `DELETE /plants/{id}` - Delete plant

#### Works
- `POST /works` - Create work order
- `GET /works` - Get all works (paginated, with filters)
- `GET /works/{id}` - Get work details
- `PATCH /works/{id}` - Update work
- `PATCH /works/{id}/close` - Mark work as completed
- `PATCH /works/{id}/invoice` - Mark work as invoiced
- `POST /works/{id}/assign-technician` - Assign technician
- `POST /works/{id}/add-reference` - Add worksite reference

**Work Filters:**
- `atixClientId`, `finalClientId`, `anyClientId`
- `sellerId`, `plantId`, `assignedTechnicianId`
- `completed`, `invoiced`
- `orderDateAfter`, `orderDateBefore`
- `name`, `bidNumber`, `orderNumber`

#### Tickets
- `POST /tickets` - Create ticket
- `GET /tickets` - Get all tickets (paginated, with filters)
- `GET /tickets/{id}` - Get ticket details
- `PATCH /tickets/{id}` - Update ticket
- `DELETE /tickets/{id}` - Delete ticket

**Ticket Filters:**
- `senderEmail`, `status`, `orderNumber`
- `name`, `description`
- `createdAtAfter`, `createdAtBefore`

#### Work Reports
- `GET /work-reports/work/{workId}` - Get work report
- `POST /work-reports/entries` - Create report entry
- `GET /work-reports/entries/work/{workId}` - Get all entries
- `PATCH /work-reports/entries/{id}` - Update entry
- `DELETE /work-reports/entries/{id}` - Delete entry

#### Worksite References
- `POST /worksite-references` - Create reference
- `GET /worksite-references` - Get all references
- `GET /worksite-references/{id}` - Get reference details
- `PATCH /worksite-references/{id}` - Update reference
- `DELETE /worksite-references/{id}` - Delete reference

#### Attachments
- `POST /attachments/{targetType}/{targetId}` - Upload attachment
- `GET /attachments/{targetType}/{targetId}` - Get attachments
- `DELETE /attachments/{attachmentId}` - Delete attachment

**Target Types**: `WORK`, `PLANT`, `TICKET`, `REPORT`

---

## Authentication & Authorization

### Bootstrap Process

On the first launch, the application automatically creates a default OWNER user (see [First Launch](#first-launch---default-admin-user) section). This solves the "chicken-and-egg" problem where only ADMIN/OWNER users can register new users.

### Authentication Flow

1. **Login**: Send credentials to `POST /auth/login`
   ```json
   {
     "email": "user@example.com",
     "password": "password123"
   }
   ```

2. **Receive JWT Token**:
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "email": "user@example.com",
     "firstName": "John",
     "lastName": "Doe",
     "role": "ADMIN"
   }
   ```

3. **Use Token**: Include in Authorization header for all protected endpoints
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

### User Roles

The system has **3 roles** with different permission levels:

| Role | Description | Permissions |
|------|-------------|-------------|
| **USER** | Basic authenticated user | Read own profile, limited access |
| **ADMIN** | Administrator | Create/edit users, manage all resources |
| **OWNER** | System owner | Full access, including deletions |

### User Types

Independent from roles, users have **3 types** based on their job function:

| Type | Description |
|------|-------------|
| **TECHNICIAN** | Field technicians who execute work orders |
| **ADMINISTRATION** | Administrative staff managing tickets and invoicing |
| **SELLER** | Sales representatives managing clients |

### Authorization Rules Examples

- Only **ADMIN** and **OWNER** can create users
- Only **OWNER** can delete resources
- Users can only update their own profile/password
- Only **Technicians** can close work orders
- Only **Administrative** users can mark works as invoiced

---

## GraphQL API

### GraphQL Endpoint

**URL**: `http://localhost:3001/api/graphql`

**GraphiQL Interface** (development): `http://localhost:3001/api/graphiql`

### Available Queries

#### Dashboard Summary

```graphql
query {
  dashboardSummary(limit: 5) {
    clientCount
    plantCount
    completedWorkCount
    pendingWorkCount
    ticketStatusCounts {
      status
      count
    }
    recentWorks {
      id
      name
      orderDate
      completed
      invoiced
    }
    recentTickets {
      id
      name
      senderEmail
      status
      createdAt
    }
  }
}
```

### Authorization

GraphQL queries require authentication. Include JWT token in headers:

```
Authorization: Bearer your-jwt-token-here
```

**Required roles**: USER, ADMIN, or OWNER

---

## Project Structure

```
AtixBackEnd/
├── src/
│   ├── main/
│   │   ├── java/marcomanfrin/atixbackend/
│   │   │   ├── controllers/          # REST API controllers
│   │   │   ├── DTO/                  # Data Transfer Objects
│   │   │   │   ├── auth/
│   │   │   │   ├── users/
│   │   │   │   ├── works/
│   │   │   │   ├── clients/
│   │   │   │   ├── plants/
│   │   │   │   ├── tickets/
│   │   │   │   ├── workReports/
│   │   │   │   └── ...
│   │   │   ├── entities/             # JPA entities
│   │   │   │   └── users/            # User inheritance hierarchy
│   │   │   ├── enums/                # Enumerations
│   │   │   ├── exceptions/           # Custom exceptions
│   │   │   ├── repositories/         # Spring Data JPA repositories
│   │   │   ├── resolvers/            # GraphQL resolvers
│   │   │   ├── security/             # Security configuration & JWT
│   │   │   ├── services/             # Business logic services
│   │   │   ├── ServiceInterfaces/    # Service interfaces
│   │   │   ├── specifications/       # JPA Specifications for filtering
│   │   │   ├── tools/                # Utility classes (Cloudinary, Mailgun)
│   │   │   └── AtixBackEndApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── graphql/
│   │           └── dashboard.graphqls # GraphQL schema
│   └── test/                         # Unit and integration tests
├── pom.xml                           # Maven dependencies
├── env.properties                    # Environment variables (not in repo)
├── AtixBackEnd API.postman_collection.json
└── README.md
```

---

## Development Notes

### Database Migrations

The application uses Hibernate with `ddl-auto = update`, which automatically updates the schema based on entity changes. For production, consider using a migration tool like Flyway or Liquibase.

### Logging

SQL queries are logged in development mode (`spring.jpa.show-sql = true`). Disable in production for performance.

### CORS Configuration

CORS origins are configured via `CORS_ALLOWED_ORIGINS` environment variable. Add your frontend URLs (comma-separated).

### Deletion Handling

When deleting resources that have relationships with other entities, the application automatically sets those references to null before performing the deletion. This prevents foreign key constraint violations and ensures data integrity.

---

## Contact & Support

For questions or issues, please contact the development team or open an issue in the repository.

---

## License

This project is part of an academic assignment and is not licensed for commercial use.
