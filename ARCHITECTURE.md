# 🏗️ System Architecture

## Overview

AutoEscalate Ops is a full-stack issue tracking system with automated workflow escalation.

```
┌─────────────────────────────────────────────────────────────────┐
│                          Frontend (Browser)                      │
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │Kanban Board  │  │ Issues List  │  │Create Issue  │          │
│  │(Drag & Drop) │  │(Filter/Sort) │  │   (Form)     │          │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘          │
│         │                  │                  │                   │
│         └──────────────────┼──────────────────┘                   │
│                            │                                       │
│                     ┌──────▼───────┐                             │
│                     │   api.js     │                             │
│                     │ (REST Client)│                             │
│                     └──────┬───────┘                             │
└────────────────────────────┼─────────────────────────────────────┘
                             │
                             │ HTTP/REST + Basic Auth
                             │
┌────────────────────────────▼─────────────────────────────────────┐
│                     Backend (Spring Boot)                         │
│                                                                   │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                    REST API Layer                          │  │
│  │  ┌──────────────────────────────────────────────────┐     │  │
│  │  │          IssueController.java                     │     │  │
│  │  │  GET    /api/issues                              │     │  │
│  │  │  GET    /api/issues/{id}                         │     │  │
│  │  │  POST   /api/issues                              │     │  │
│  │  │  PATCH  /api/issues/{id}                         │     │  │
│  │  └──────────────────┬───────────────────────────────┘     │  │
│  └─────────────────────┼───────────────────────────────────────┘  │
│                        │                                           │
│  ┌─────────────────────▼───────────────────────────────────────┐  │
│  │                   Service Layer                              │  │
│  │  ┌────────────────────────────────────────────────────┐    │  │
│  │  │           IssueService.java                        │    │  │
│  │  │  - Business logic                                  │    │  │
│  │  │  - Transaction management                          │    │  │
│  │  │  - Data validation                                 │    │  │
│  │  └──────────────┬──────────────────┬──────────────────┘    │  │
│  └─────────────────┼──────────────────┼───────────────────────┘  │
│                    │                  │                           │
│         ┌──────────▼────────┐    ┌───▼──────────────┐           │
│         │  IssueRepository  │    │    SPAClient     │           │
│         │   (Spring Data    │    │ (Workflow Trigger)│           │
│         │      JPA)         │    └───┬──────────────┘           │
│         └──────────┬────────┘        │                           │
│                    │                 │                           │
│  ┌─────────────────▼───────┐    ┌───▼──────────────┐           │
│  │   Persistence Layer      │    │  Integration     │           │
│  │  ┌──────────────────┐   │    │  ┌─────────────┐ │           │
│  │  │  Issue Entity    │   │    │  │OAuth Client │ │           │
│  │  │  (JPA/Hibernate) │   │    │  │Credentials  │ │           │
│  │  └──────────────────┘   │    │  └─────────────┘ │           │
│  └─────────────┬────────────┘    └───┬──────────────┘           │
└────────────────┼─────────────────────┼──────────────────────────┘
                 │                     │
        ┌────────▼────────┐    ┌───────▼────────────────┐
        │  H2 Database    │    │  SAP BPA Workflow API   │
        │  (In-Memory)    │    │  - OAuth Token          │
        │                 │    │  - Workflow Instances   │
        └─────────────────┘    └─────────────────────────┘
```

## Data Flow

### 1. Issue Creation Flow

```
User (Browser)
    │
    │ 1. Fill form & submit
    ▼
Frontend (app.js)
    │
    │ 2. POST /api/issues (with auth)
    ▼
Backend (IssueController)
    │
    │ 3. Validate & delegate
    ▼
Service (IssueService)
    │
    ├─► 4a. Save to database
    │       │
    │       ▼
    │   IssueRepository → H2 Database
    │
    └─► 4b. Trigger workflow (async)
            │
            ▼
        SPAClient
            │
            ├─► 5. Fetch OAuth token
            │       POST /oauth/token
            │       (client credentials)
            │
            └─► 6. Start workflow
                    POST /public/workflow/rest/v1/workflow-instances
                    (with bearer token + API key)
                    │
                    ▼
                SAP BPA Workflow Engine
```

### 2. Issue Update Flow (Drag & Drop)

```
User drags card in Kanban
    │
    │ Drop on new column
    ▼
Frontend (app.js)
    │
    │ PATCH /api/issues/{id}
    │ { "status": "InProgress" }
    ▼
Backend (IssueController)
    │
    ▼
Service (IssueService)
    │
    │ Update timestamps if needed:
    │ - acknowledgedAt (when → InProgress)
    │ - resolvedAt (when → Resolved/Closed)
    ▼
Repository (IssueRepository)
    │
    ▼
Database (H2)
    │
    │ Return updated issue
    ▼
Frontend refreshes Kanban board
```

## Component Details

### Frontend Components

**1. api.js - REST Client**
- Handles all HTTP communication
- Adds authentication headers automatically
- Error handling and logging

**2. app.js - Application Logic**
- View management (Kanban, List, Create, Detail)
- State management
- Event handling (drag & drop, forms, filters)
- DOM manipulation

**3. styles.css - UI Styling**
- Responsive design
- Color scheme for priority/status badges
- Kanban board layout
- Form styling

### Backend Components

**1. Controllers (REST Layer)**
- `IssueController` - HTTP request handling
- Request validation
- Response formatting
- HTTP status codes

**2. Services (Business Logic)**
- `IssueService` - Issue CRUD operations
- `SPAClient` - Workflow integration
- Transaction management
- Data transformation

**3. Repositories (Data Access)**
- `IssueRepository` - JPA repository
- Custom queries (findByStatus, findByPriority)
- Database abstraction

**4. Entities (Data Models)**
- `Issue` - Main domain entity
- JPA annotations for ORM mapping
- Relationships and constraints

**5. DTOs (Data Transfer Objects)**
- `IssueCreateRequest` - Creation payload
- `IssueUpdateRequest` - Update payload
- `IssueResponse` - API response format

**6. Configuration**
- `SPAProperties` - Workflow credentials
- `WebClientConfig` - HTTP client setup
- `Application` - Spring Boot entry point

## Technology Stack Details

### Backend Technologies

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Framework | Spring Boot 3.5.6 | Web application framework |
| CAP | SAP CAP 4.4.1 | Cloud application model |
| ORM | Hibernate/JPA | Database mapping |
| Database | H2 (in-memory) | Data persistence |
| HTTP Client | Spring WebFlux | Reactive HTTP calls |
| Security | Spring Security | Authentication/Authorization |
| Build Tool | Maven 3.9 | Dependency & build management |
| Java Version | Java 21 | Programming language |

### Frontend Technologies

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Language | JavaScript (ES6+) | Application logic |
| Modules | ES6 Modules | Code organization |
| HTTP | Fetch API | REST communication |
| UI | Vanilla HTML5/CSS3 | User interface |
| Patterns | MVC-like | Architecture pattern |

### Integration

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Auth | OAuth 2.0 | Client credentials flow |
| Protocol | REST/HTTP | API communication |
| Format | JSON | Data serialization |
| Headers | Basic Auth + Bearer | Authentication |

## Security Architecture

```
┌──────────────────────────────────────────────────────┐
│              Frontend (Browser)                       │
│  - No secrets stored                                  │
│  - Basic auth credentials in code (demo only)        │
└───────────────────┬──────────────────────────────────┘
                    │
                    │ HTTPS (production)
                    │ HTTP (development)
                    │
┌───────────────────▼──────────────────────────────────┐
│            Backend (Spring Boot)                      │
│                                                       │
│  ┌─────────────────────────────────────────────┐    │
│  │   Spring Security (Disabled for demo)       │    │
│  │   - Mock user authentication                │    │
│  │   - Basic auth validation                   │    │
│  └─────────────────────────────────────────────┘    │
│                                                       │
│  ┌─────────────────────────────────────────────┐    │
│  │   Credentials Management                    │    │
│  │   - SAP BPA credentials in application.yaml │    │
│  │   - OAuth client ID/secret                  │    │
│  │   - API key                                 │    │
│  └─────────────────────────────────────────────┘    │
└───────────────────┬──────────────────────────────────┘
                    │
                    │ OAuth 2.0
                    │ Client Credentials
                    │
┌───────────────────▼──────────────────────────────────┐
│          SAP Build Process Automation                 │
│  - OAuth token validation                            │
│  - API key validation                                │
│  - Workflow authorization                            │
└──────────────────────────────────────────────────────┘
```

## Deployment Architecture

### Development (Current)

```
Developer Machine
├── H2 Database (in-memory)
├── Spring Boot (port 8080)
│   ├── REST API
│   ├── Static Frontend
│   └── H2 Console
└── External: SAP BPA (cloud)
```

### Production (Recommended)

```
Load Balancer (HTTPS)
    │
    ├─► Frontend Tier (CDN/Nginx)
    │       └── Static assets (HTML/CSS/JS)
    │
    └─► Backend Tier (Multiple instances)
            ├── Spring Boot instances
            └── Persistent Database (PostgreSQL/SAP HANA)
                    │
                    └─► SAP BPA (cloud)
```

## Database Schema

```sql
CREATE TABLE issues (
    id                  UUID PRIMARY KEY,
    title               VARCHAR(100) NOT NULL,
    description         VARCHAR(1000),
    type                VARCHAR(50),
    priority            VARCHAR(20),
    status              VARCHAR(20),
    severity            INTEGER,
    location            VARCHAR(100),
    reported_at         TIMESTAMP,
    acknowledged_at     TIMESTAMP,
    resolved_at         TIMESTAMP,
    resolution_comment  VARCHAR(1000)
);

-- Indexes (auto-created by JPA)
CREATE INDEX idx_status ON issues(status);
CREATE INDEX idx_priority ON issues(priority);
```

## API Contract

### Request/Response Examples

**Create Issue:**
```http
POST /api/issues
Authorization: Basic YXV0aGVudGljYXRlZDo=
Content-Type: application/json

{
  "title": "Conveyor belt jammed",
  "description": "Belt stops intermittently",
  "type": "Breakdown",
  "priority": "High",
  "location": "Plant A / Line 3",
  "severity": 4
}

Response: 201 Created
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "title": "Conveyor belt jammed",
  "status": "New",
  "reportedAt": "2025-11-22T21:44:20.262660",
  ...
}
```

## Scalability Considerations

### Current Limitations
- In-memory database (single instance)
- Synchronous workflow triggering
- No caching layer
- No message queue

### Future Improvements
1. **Database:** PostgreSQL with connection pooling
2. **Caching:** Redis for frequently accessed data
3. **Async:** Message queue (RabbitMQ/Kafka) for workflows
4. **Scaling:** Kubernetes for horizontal scaling
5. **Monitoring:** Prometheus + Grafana
6. **Logging:** ELK stack

---

This architecture provides a solid foundation for a production-grade issue tracking system with workflow automation capabilities.
