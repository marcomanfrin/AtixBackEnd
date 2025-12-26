# SoftwareOps - Field Service Management System

A comprehensive Spring Boot application for managing field service operations, including ticket management, work scheduling, time tracking, and client relationship management.

## 📋 Table of Contents

- [Overview](#overview)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Features](#features)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Authentication](#authentication)

---

## 🎯 Overview

SoftwareOps is an enterprise-grade backend system designed to streamline field service operations. It provides a complete solution for:

- **Ticket Management**: Track customer support requests from creation to resolution
- **Work Scheduling**: Assign and manage work orders for field technicians
- **Time Tracking**: Record work hours and generate detailed reports
- **Client Management**: Maintain client and plant information
- **Document Management**: Upload and link attachments to various entities

The system supports both **REST API** and **GraphQL** for flexible data access patterns.

---

## 🛠 Technologies

### Backend Framework
- **Java 21**
- **Spring Boot 4.0.0**
  - Spring Web MVC
  - Spring Data JPA
  - Spring Security
  - Spring Boot Validation
  - Spring GraphQL
  - Spring Boot DevTools

### Database
- **PostgreSQL** (production database)
- **Hibernate/JPA** (ORM)

### Security
- **JWT** (JSON Web Tokens) for stateless authentication
- **BCrypt** password encryption (strength: 12)
- **Spring Security** with role-based access control

### Third-Party Integrations
- **Cloudinary** - Cloud-based image and file storage
- **Mailgun** - Transactional email service

### Build Tool
- **Maven** (Apache Maven)

---

## 📦 Prerequisites

Before running this application, ensure you have:

- **Java 21** or higher
- **Maven 3.8+**
- **PostgreSQL 14+** (running locally or remotely)
- **Cloudinary Account** (for file uploads)
- **Mailgun Account** (for email notifications)

---

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd SoftwareOps
```

### 2. Create PostgreSQL Database

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE softwareops;

# Exit psql
\q
```

### 3. Configure Environment Variables

Create a file named `env.properties` in the project root (see [Configuration](#configuration) section).

### 4. Build the Project

```bash
mvn clean install
```

---

## ⚙️ Configuration

### Environment Variables

Create an `env.properties` file in the project root with the following configuration:

```properties
# Database Configuration
PG_DB_NAME=softwareops
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

# JWT Security
JWT_SECRET=your_jwt_secret_key_minimum_256_bits

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200
```

### Important Notes

⚠️ **Never commit `env.properties` to version control!** (It's already in `.gitignore`)

💡 Use `env.example.properties` as a template

🔐 Generate a strong JWT secret:
```bash
openssl rand -base64 64
```

---

## ▶️ Running the Application

### Development Mode

```bash
mvn spring-boot:run
```

### Production Mode

```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/SoftwareOps-0.0.1-SNAPSHOT.jar
```

The application will start on **http://localhost:3001**

### Verify Installation

```bash
curl http://localhost:3001/api/auth/login
```

---

## 📚 API Documentation

### REST API

Base URL: `http://localhost:3001/api`

All API endpoints (except `/auth/*`) require JWT authentication via the `Authorization: Bearer <token>` header.

#### Available Endpoints

| Category | Endpoint | Description |
|----------|----------|-------------|
| **Auth** | `POST /auth/login` | User login |
| **Auth** | `POST /auth/register` | User registration |
| **Users** | `GET /users` | List users (paginated) |
| **Users** | `GET /users/{id}` | Get user by ID |
| **Users** | `PATCH /users/{id}` | Update user |
| **Users** | `DELETE /users/{id}` | Delete user (OWNER only) |
| **Users** | `PATCH /users/{id}/password` | Change password |
| **Users** | `PATCH /users/{id}/avatar` | Upload profile image |
| **Clients** | `POST /clients` | Create client |
| **Clients** | `GET /clients` | List clients (paginated) |
| **Clients** | `PATCH /clients/{id}` | Update client |
| **Plants** | `POST /plants` | Create plant |
| **Plants** | `GET /plants` | List plants (paginated) |
| **Plants** | `POST /plants/{id}/works` | Create work from plant |
| **Tickets** | `POST /tickets` | Create ticket |
| **Tickets** | `GET /tickets` | List tickets (paginated, filterable) |
| **Tickets** | `POST /tickets/{id}/close` | Close ticket |
| **Tickets** | `POST /tickets/{id}/works` | Create work from ticket |
| **Works** | `GET /works` | List works (paginated, filterable) |
| **Works** | `POST /works/{id}/assignments` | Assign technician |
| **Works** | `PATCH /works/{id}/complete` | Complete work |
| **Tasks** | `POST /works/{workId}/tasks` | Create task |
| **Tasks** | `GET /works/{workId}/tasks` | List tasks |
| **Reports** | `POST /works/{workId}/reports` | Create work report |
| **Reports** | `POST /reports/{id}/finalize` | Finalize report |
| **Attachments** | `POST /attachments/{type}/{id}` | Upload attachment |

### GraphQL API

Endpoint: `http://localhost:3001/graphql`
GraphiQL UI: `http://localhost:3001/graphiql`

**Example Query:**
```graphql
query {
  dashboardSummary(limit: 5) {
    clientsCount
    plantsCount
    worksByStatus {
      status
      count
    }
    ticketsByStatus {
      status
      count
    }
    lastWorks {
      id
      status
      createdAt
    }
  }
}
```

### Postman Collection

Import the included **`SoftwareOps API.postman_collection.json`** file into Postman for a complete set of API requests with examples.

#### Setup in Postman:
1. Import the collection
2. Set environment variables:
   - `baseUrl`: `http://localhost:3001/api`
   - `token`: (obtained from `/auth/login`)
3. Login to get JWT token
4. Token is automatically included in authenticated requests

---

## ✨ Features

### Core Functionality

#### 1. User Management
- User registration with email verification
- JWT-based authentication
- Role-based access control (OWNER, ADMIN, USER)
- User types: Administrative and Technician
- Profile image upload via Cloudinary
- Password change with validation

#### 2. Client & Plant Management
- Client CRUD operations with type classification (DIRECT/FINAL)
- Plant management with order tracking
- Invoice tracking for plants
- Search functionality for clients and plants
- Pagination support

#### 3. Ticket System
- Create support tickets linked to clients and plants
- Ticket status workflow: OPEN → IN_PROGRESS → RESOLVED → CLOSED
- Convert tickets to work orders
- Filter by status, client, or plant
- Pagination and search

#### 4. Work Management
- Two types of work:
  - **WorkFromTicket**: Reactive work (from support tickets)
  - **WorkFromPlant**: Proactive work (scheduled maintenance)
- Work status lifecycle: DRAFT → SCHEDULED → IN_PROGRESS → COMPLETED/CANCELLED
- Progress tracking (percentage)
- Technician assignment with roles (LEAD/ASSISTANT)

#### 5. Task Management
- Create checklist items for work orders
- Task status tracking: PENDING → IN_PROGRESS → COMPLETED
- Completion rate calculation

#### 6. Time Tracking & Reporting
- Work reports with detailed time entries
- Hour tracking per activity
- Report finalization workflow
- Invoice tracking

#### 7. File Attachments
- Upload files (images/documents) to Cloudinary
- Link attachments to tickets, works, plants, or reports
- Polymorphic attachment system

#### 8. Dashboard & Analytics (GraphQL)
- Real-time statistics by work/ticket status
- Client and plant counts
- Recent activity feeds
- Customizable data retrieval

### Security Features

- **Password Requirements**: Minimum 8 characters, uppercase, lowercase, and number
- **JWT Token Expiration**: 4 weeks
- **BCrypt Encryption**: Strength 12 for password hashing
- **CORS Configuration**: Configurable allowed origins
- **Role-Based Authorization**: Fine-grained access control
- **Stateless Authentication**: No server-side sessions

---

## 📁 Project Structure

```
src/main/java/marcomanfrin/softwareops/
├── controllers/          # REST API endpoints
│   ├── AuthController
│   ├── UsersController
│   ├── ClientsController
│   ├── TicketsController
│   ├── WorksController
│   └── ...
├── resolvers/           # GraphQL query handlers
│   └── DashboardGraphQLController
├── services/            # Business logic layer
│   ├── UserService
│   ├── TicketService
│   ├── WorkService
│   └── ...
├── repositories/        # Data access layer (JPA)
│   ├── UserRepository
│   ├── TicketRepository
│   └── ...
├── entities/            # JPA entities (domain models)
│   ├── User (abstract)
│   │   ├── AdministrativeUser
│   │   └── TechnicianUser
│   ├── Work (abstract)
│   │   ├── WorkFromTicket
│   │   └── WorkFromPlant
│   ├── Ticket
│   ├── Client
│   └── ...
├── DTO/                 # Data Transfer Objects
│   ├── users/
│   ├── tickets/
│   ├── works/
│   └── ...
├── enums/              # Enumerations
│   ├── UserRole
│   ├── TicketStatus
│   ├── WorkStatus
│   └── ...
├── exceptions/         # Custom exceptions
├── security/           # Security configuration
│   ├── SecurityConfig
│   ├── JWTAuthFilter
│   ├── JWTTools
│   └── SecurityService
└── tools/              # Utility classes
    ├── CloudinaryConfig
    └── MailgunSender

src/main/resources/
├── application.properties
└── graphql/
    └── dashboard.graphqls
```

---

## 🗄 Database Schema

### Main Entities

#### Users
- **User** (abstract base class)
  - Single Table Inheritance (`user_type` discriminator)
  - **AdministrativeUser**: Office staff
  - **TechnicianUser**: Field technicians
- Roles: OWNER, ADMIN, USER

#### Clients & Plants
- **Client**: Customer information (DIRECT or FINAL type)
- **Plant**: Customer installations with order tracking

#### Tickets & Works
- **Ticket**: Support requests with status workflow
- **Work** (abstract base class)
  - Joined Table Inheritance
  - **WorkFromTicket**: Work generated from tickets
  - **WorkFromPlant**: Scheduled maintenance work
- **WorkAssignment**: Links technicians to work orders

#### Tasks & Reports
- **Task**: Checklist items for work orders
- **WorkReport**: Time tracking and invoicing
- **WorkReportEntry**: Individual time entries

#### Attachments
- **Attachment**: File metadata (Cloudinary)
- **AttachmentLink**: Polymorphic links to entities

### Key Relationships

```
Client 1──N Plant
Client 1──N Ticket
Plant  1──N Ticket
Ticket 1──1 WorkFromTicket
Plant  1──N WorkFromPlant
Work   1──N Task
Work   1──N WorkAssignment ──N User(Technician)
Work   1──1 WorkReport
WorkReport 1──N WorkReportEntry
```

---

## 🔐 Authentication

### Registration

```bash
POST /api/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "userName": "johndoe",
  "email": "john.doe@example.com",
  "password": "SecurePass123",
  "role": "USER",
  "userType": "TECHNICIAN"
}
```

### Login

```bash
POST /api/auth/login
Content-Type: application/json

{
  "userName": "johndoe",
  "password": "SecurePass123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Authenticated Requests

Include the JWT token in the `Authorization` header:

```bash
GET /api/users
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### User Roles & Permissions

| Role | Permissions |
|------|-------------|
| **OWNER** | Full system access, can delete users and plants |
| **ADMIN** | Administrative access, user management |
| **USER** | Basic access, view own data |

**User Types:**
- **ADMINISTRATIVE**: Office staff, administrative tasks
- **TECHNICIAN**: Field workers, assigned to work orders

---

## 📝 Notes

### Password Requirements

All passwords must meet the following criteria:
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number

### Pagination

List endpoints support pagination with query parameters:
- `page`: Page number (0-indexed, default: 0)
- `size`: Items per page (default: 20, max: 100)

Example: `GET /api/users?page=0&size=20`

### File Uploads

File uploads use `multipart/form-data` encoding:
```bash
POST /api/users/{id}/avatar
Content-Type: multipart/form-data

avatar: <file>
```

---

## 🤝 Contributing

This is an academic project for Backend Programming course evaluation.

---

## 📄 License

Academic project - All rights reserved.

---

## 👨‍💻 Author

**Marco Manfrin**
Backend Programming - Y2S1
University Project - 2024/2025

---

## 📞 Support

For questions or issues, please refer to the Postman collection for detailed API examples.
