# AutoEscalate Ops - Issue Tracking & Workflow Automation System

A comprehensive issue tracking system integrated with SAP Build Process Automation (BPA) for automated escalation workflows in manufacturing operations.

## 🎯 Overview

AutoEscalate Ops is a full-stack application that helps manufacturing teams track and manage operational issues. When critical issues are reported (machine breakdowns, quality defects, safety concerns), the system automatically triggers escalation workflows in SAP Build Process Automation.

### Key Features

- 📋 **Issue Management** - Create, track, and update operational issues
- 🎨 **Kanban Board** - Visual drag-and-drop interface for issue status management
- 🔄 **Automated Workflows** - Automatic escalation via SAP BPA when issues are created
- 📊 **Filtering & Search** - Filter issues by status, priority, and search by keywords
- 🔍 **Detailed Views** - Comprehensive issue details with timeline tracking
- ⚡ **Real-time Updates** - State consistency between frontend and backend

## 🏗️ Architecture

### Technology Stack

**Backend:**
- Java 21 + Spring Boot 3.5.6
- SAP Cloud Application Programming Model (CAP)
- Spring Data JPA with H2 Database
- Spring WebFlux for reactive HTTP calls
- Spring Security with mock authentication

**Frontend:**
- Vanilla JavaScript (ES6 Modules)
- HTML5 + CSS3
- Fetch API for REST communication
- Responsive design

**Integration:**
- SAP Build Process Automation (BPA)
- OAuth 2.0 Client Credentials flow
- RESTful API integration

### Project Structure

```
SAP-Build/
├── app/                          # Frontend applications
│   └── issuetracker/
│       └── webapp/
│           ├── index.html        # Main UI
│           ├── styles.css        # Styles
│           └── js/
│               ├── api.js        # Backend API client
│               └── app.js        # Application logic
├── srv/                          # Backend service
│   ├── pom.xml                   # Maven configuration
│   └── src/main/
│       ├── java/customer/autoescalate_ops/
│       │   ├── Application.java
│       │   ├── config/          # Configuration classes
│       │   ├── controller/      # REST controllers
│       │   ├── service/         # Business logic
│       │   ├── entity/          # JPA entities
│       │   ├── dto/             # Data transfer objects
│       │   └── repository/      # Data repositories
│       └── resources/
│           ├── application.yaml  # Application config
│           └── schema-h2.sql    # Database schema
├── db/                          # Database models (CAP)
└── README.md                    # This file
```

## 🚀 Quick Start

### Prerequisites

- **Java 21** or higher
- **Maven 3.9+**
- **Node.js 18+** (for CAP tools)
- **Git**

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/raj7vasani/autoescalate-ops.git
   cd SAP-Build
   ```

2. **Run the setup script**
   ```bash
   chmod +x setup.sh
   ./setup.sh
   ```

   Or manually:
   ```bash
   # Install dependencies
   npm install

   # Build and start the application
   cd srv
   mvn clean install
   mvn spring-boot:run
   ```

3. **Access the application**
   - **Frontend UI:** http://localhost:8080/issuetracker/webapp/index.html
   - **API Base:** http://localhost:8080/api/issues
   - **H2 Console:** http://localhost:8080/h2-console

4. **Login Credentials**
   - **Username:** `authenticated`
   - **Password:** (leave blank)

## 📖 Usage Guide

### Creating an Issue

1. Navigate to the **Create Issue** tab
2. Fill in the required fields:
   - **Title** - Brief description of the issue
   - **Description** - Detailed explanation
   - **Type** - Category (Machine Breakdown, Quality Defect, etc.)
   - **Priority** - Low, Medium, High, or Critical
   - **Location** - Plant/Area/Line location
   - **Severity** - Numeric scale 1-5
3. Click **Create Issue**
4. The issue is saved and a workflow is automatically triggered in SAP BPA (if servers are online)

### Managing Issues

**Kanban Board View:**
- Drag and drop cards between columns to update status
- Filter by priority using the dropdown
- Search by title or description

**List View:**
- View all issues in a table format
- Filter by status and priority
- Click any row to view detailed information

**Issue Detail View:**
- View complete issue information
- Update status using the dropdown
- See timeline (reported, acknowledged, resolved dates)

## 🔧 Configuration

### Backend Configuration

All configuration is in `srv/src/main/resources/application.yaml`:

```yaml
# Database Configuration
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password:

# SAP Build Process Automation
spa:
  baseUrl: https://spa-api-gateway-bpi-eu-prod.cfapps.eu10.hana.ondemand.com
  environmentId: hackatum
  apiKey: QBvlzsXZoiatJbK9cca98l0tQRCKxSss
  definitionId: IssueEscalationProcess
  tokenUrl: https://hackatum.authentication.eu10.hana.ondemand.com/oauth/token
  clientId: sb-adcf8c2e-99c5-4ae2-9e28-c0ef5f56e78a!b605607|xsuaa!b120249
  clientSecret: dc8ac31c-f9c3-4fe4-97c1-b951daadfeb7$tHte333ltNrW_v7ws864AkKLQGhI0-uX7l93Pxyq7BU=
```

**Note:** These are hackathon credentials. For production, use environment variables.

### Frontend Configuration

The API base URL is configured in `app/issuetracker/webapp/js/api.js`:

```javascript
const API_BASE_URL = '/api/issues';
```

Authentication is automatically added to all requests.

## 🔐 Security

**Current Setup (Development/Hackathon):**
- Spring Security is disabled for ease of testing
- Mock users are configured via SAP CAP framework
- Basic Authentication required for all endpoints
- Credentials: `authenticated` (no password)

**Production Recommendations:**
1. Enable Spring Security
2. Implement proper OAuth 2.0 or SAML authentication
3. Use environment variables for all credentials
4. Enable HTTPS/TLS
5. Implement CORS policies
6. Add rate limiting
7. Use secure session management

## 🔌 API Documentation

### Endpoints

#### `GET /api/issues`
Fetch all issues with optional filters.

**Query Parameters:**
- `status` (optional) - Filter by status: New, InProgress, Resolved, Closed
- `priority` (optional) - Filter by priority: Low, Medium, High, Critical

**Response:**
```json
[
  {
    "id": "uuid",
    "title": "Conveyor belt jammed",
    "description": "Belt stops intermittently",
    "type": "Breakdown",
    "priority": "High",
    "status": "New",
    "severity": 4,
    "location": "Plant A / Line 3",
    "reportedAt": "2025-11-22T21:44:20.262660",
    "acknowledgedAt": null,
    "resolvedAt": null,
    "resolutionComment": null
  }
]
```

#### `GET /api/issues/{id}`
Fetch a single issue by ID.

**Response:** Single issue object (same structure as above)

#### `POST /api/issues`
Create a new issue and trigger workflow.

**Request Body:**
```json
{
  "title": "Conveyor belt jammed",
  "description": "Belt stops intermittently on Line 3",
  "type": "Breakdown",
  "priority": "High",
  "location": "Plant A / Line 3",
  "severity": 4
}
```

**Response:** Created issue object with generated `id` and `reportedAt`

#### `PATCH /api/issues/{id}`
Update an issue (typically status).

**Request Body:**
```json
{
  "status": "InProgress",
  "resolutionComment": "Maintenance team assigned"
}
```

**Response:** Updated issue object

## 🔄 SAP BPA Integration

When an issue is created, the backend automatically:

1. **Fetches OAuth Token** - Uses client credentials flow to authenticate with SAP BPA
2. **Triggers Workflow** - Calls the workflow API with issue details
3. **Logs Results** - Records success or failure in application logs

**Workflow Payload Structure:**
```json
{
  "definitionId": "IssueEscalationProcess",
  "context": {
    "startEvent": {
      "issueid": "uuid",
      "title": "Issue title",
      "description": "Issue description",
      "type": "Issue type",
      "priority": "Priority level",
      "location": "Location string",
      "severity": 4,
      "date": "2025-11-22T20:55:00Z"
    }
  }
}
```

**Monitoring:**
Check application logs for workflow trigger status:
```
INFO - Starting SPA workflow for issue: <uuid>
INFO - Successfully started workflow for issue <uuid>
```

Or errors:
```
ERROR - Failed to start workflow for issue <uuid>: <error message>
```

## 🧪 Testing

### Manual Testing

**Test Issue Creation:**
```bash
curl -X POST http://localhost:8080/api/issues \
  -u "authenticated:" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Issue",
    "description": "Testing the system",
    "type": "Breakdown",
    "priority": "High",
    "location": "Test Location",
    "severity": 3
  }'
```

**Test Issue Retrieval:**
```bash
curl -u "authenticated:" http://localhost:8080/api/issues
```

**Test Issue Update:**
```bash
curl -X PATCH http://localhost:8080/api/issues/{id} \
  -u "authenticated:" \
  -H "Content-Type: application/json" \
  -d '{"status": "InProgress"}'
```

### Automated Tests

Run backend tests:
```bash
cd srv
mvn test
```

## 🐛 Troubleshooting

### Application won't start
- Ensure Java 21+ is installed: `java -version`
- Check if port 8080 is available: `lsof -i :8080`
- Verify Maven is installed: `mvn -version`

### Frontend shows authentication popup repeatedly
- Clear browser cache
- Check that basic auth header is included in requests
- Verify application is running on port 8080

### Workflow not triggering
- Check application logs for errors
- Verify SAP BPA servers are online
- Confirm OAuth credentials are correct in `application.yaml`
- Test OAuth endpoint manually with curl

### Issues not persisting
- Check H2 console: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`, Password: (blank)

### CORS errors in browser console
- Ensure you're accessing the app through `localhost:8080`
- Check that the backend is running
- Verify Spring Security CORS configuration

## 📝 Development

### Adding New Features

**Backend - Add a new endpoint:**
1. Create DTO in `srv/src/main/java/customer/autoescalate_ops/dto/`
2. Add method to `IssueService.java`
3. Add endpoint to `IssueController.java`

**Frontend - Add a new view:**
1. Add view div in `renderApp()` in `app.js`
2. Create render function (e.g., `renderMyView()`)
3. Add navigation tab
4. Implement view switching logic

### Database Schema Changes

1. Modify entity in `srv/src/main/java/customer/autoescalate_ops/entity/`
2. Update JPA will auto-generate schema (DDL auto-update enabled)
3. For production, use proper migration tools (Flyway/Liquibase)

## 🤝 Contributing

This project was created for a hackathon. If you'd like to contribute:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

- **SAP Build Process Automation** - For workflow automation capabilities
- **SAP Cloud Application Programming Model** - For backend framework
- **HackaTUM** - For hosting the hackathon

## 📞 Support

For issues or questions:
- Check the troubleshooting section above
- Review application logs in the console
- Open an issue on GitHub

---

**Built with ❤️ for HackaTUM 2025**
