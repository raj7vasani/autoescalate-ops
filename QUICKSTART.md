# 🚀 Quick Start Guide

Get AutoEscalate Ops running in **3 simple steps**!

## Prerequisites

Make sure you have installed:
- **Java 21+** - [Download](https://adoptium.net/)
- **Maven 3.9+** - [Download](https://maven.apache.org/download.cgi)
- **Git** - [Download](https://git-scm.com/)

## Installation

### Option 1: Automated Setup (Recommended)

```bash
# Clone the repository
git clone <your-repo-url>
cd SAP-Build

# Run the setup script
chmod +x setup.sh
./setup.sh
```

That's it! The application will start automatically.

### Option 2: Manual Setup

```bash
# Clone the repository
git clone <your-repo-url>
cd SAP-Build

# Install dependencies (if package.json exists)
npm install

# Build and run
cd srv
mvn clean install
mvn spring-boot:run
```

## Access the Application

Once started, open your browser:

**🌐 Frontend:** http://localhost:8080/issuetracker/webapp/index.html

**Login Credentials:**
- Username: `authenticated`
- Password: _(leave blank)_

## What's Next?

### Create Your First Issue

1. Click on the **"Create Issue"** tab
2. Fill in the form:
   - **Title:** "Test Issue"
   - **Description:** "Testing the application"
   - **Type:** Select "Machine Breakdown"
   - **Priority:** Select "High"
   - **Location:** "Test Plant"
   - **Severity:** Enter "3"
3. Click **"Create Issue"**

Your issue is now created and the SAP BPA workflow is triggered (if servers are online)!

### Explore the Kanban Board

1. Click on **"Kanban Board"** tab
2. See your issues organized by status
3. **Drag and drop** cards between columns to update status

### View All Issues

1. Click on **"Issues List"** tab
2. Use filters to find specific issues
3. Click any row to see detailed information

## Common Commands

```bash
# Stop the application
Ctrl + C

# Restart the application
cd srv
mvn spring-boot:run

# View logs
# They appear in the terminal where you ran mvn spring-boot:run

# Access H2 Database Console
# Browser: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:testdb
# Username: sa
# Password: (blank)
```

## Testing the API

You can also interact with the backend directly:

```bash
# Get all issues
curl -u "authenticated:" http://localhost:8080/api/issues

# Create an issue
curl -X POST http://localhost:8080/api/issues \
  -u "authenticated:" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "API Test",
    "description": "Testing via API",
    "type": "Breakdown",
    "priority": "High",
    "location": "Test",
    "severity": 3
  }'
```

## Need Help?

- 📖 Full documentation: See [README.md](README.md)
- 🐛 Having issues? Check the [Troubleshooting](README.md#-troubleshooting) section
- 💡 Check application logs in your terminal

## What's Running?

When you start the application, these services are available:

| Service | URL | Description |
|---------|-----|-------------|
| Frontend UI | http://localhost:8080/issuetracker/webapp/index.html | Main application |
| REST API | http://localhost:8080/api/issues | Backend API |
| H2 Console | http://localhost:8080/h2-console | Database viewer |
| CAP Index | http://localhost:8080/ | Service info |

---

**Happy Tracking! 🎉**
