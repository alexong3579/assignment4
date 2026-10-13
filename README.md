# TaskFlow API

TaskFlow API is a RESTful task management service built with **Spring Boot** and **Java 21**, containerized with a multi-stage Docker build, and deployed to production on **Railway**.

---

## Deployment & Live Verification

### Live Application URL
The API is deployed and accessible at:

```
https://assignment4-production-6a4a.up.railway.app/api/tasks
```

### Deployment Screenshots

#### 1. Railway Dashboard
Deployment status and active service overview on Railway:
![alt text](dashboard.png)


#### 2. Live Application
API endpoint response verifying that the deployed service is active and handling requests:
![alt text](application.png)

---

## Project Status & Notes

### Deployment Status
- **Tasks Completed:** Multi-stage Docker containerization, local container testing, GitHub integration, and Railway cloud deployment.
- **Errors / Issues:** None.

### Stretch Goals (Optional)
- **Selection:** Option 1 (CI pipeline), Option 4 (versioned Docker images) and Option 5 (security checks)
- **Notes:** Baseline assignment requirements completed. The three options share one GitHub Actions workflow, [`.github/workflows/ci.yml`](.github/workflows/ci.yml). Details are in the sections below.

---

## Stretch Goal: CI Pipeline (Option 1)

A GitHub Actions workflow runs on every push to `main` (and on version tags). It has three jobs:

1. **`test`** runs `./mvnw verify` (10 unit and integration tests).
2. **`security-scan`** runs OWASP Dependency-Check and fails the build on any vulnerability with CVSS 9.0 or higher.
3. **`docker`** builds the image and pushes it to Docker Hub, only if the first two jobs pass.

Docker Hub credentials and the NVD API key are stored as GitHub Actions secrets (`DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `NVD_API_KEY`) and are never in the code.

Successful pipeline run: https://github.com/alexong3579/assignment4/actions/runs/38417142859

This pipeline runs alongside the Railway deployment pipeline, which redeploys automatically on each push to `main`.

---

## Stretch Goal: Security Checks (Option 5)

### Dependency scan (OWASP Dependency-Check 13.0.0)
The scan runs in the `security-scan` CI job. The full HTML report is uploaded as the `dependency-check-report` artifact on each run.

**What it found:** the first complete scan reported one vulnerable dependency, `tomcat-embed-core` 11.0.24, which Spring Boot 4.1.1 brings in by default. It had 11 known CVEs, 4 of them critical:

| CVE | CVSS |
| :--- | :--- |
| CVE-2026-65905 | 9.8 |
| CVE-2026-65637 | 9.8 |
| CVE-2026-68525 | 9.1 |
| CVE-2026-65182 | 9.1 |

The other 7 CVEs on the same jar scored below 9.0. The build failed on the four critical ones, which is what the CVSS 9.0 gate is meant to do.

**What I fixed:** I overrode the Spring Boot managed version in `pom.xml` with `<tomcat.version>11.0.27</tomcat.version>`. NVD lists the affected range as Tomcat 11.0.20 through 11.0.24, and 11.0.27 is the latest 11.0.x release. After the change, all 10 tests passed and the scan passed.

### Secrets check
No passwords, tokens or API keys are committed. The only match in a search of the code is the H2 database password in `application.properties`, which is intentionally empty for the in-memory database. The CI credentials live only in GitHub Actions secrets.

### Issues I left as they are
- **Build gate is CVSS 9.0 or higher.** Findings below that score do not fail the build. They are still listed in the report artifact.
- **Sonatype OSS Index analyzer is disabled.** It now requires credentials, so only the NVD data source is used.
- **No container image scan.** I scanned dependencies only. Scanning the Docker image (for example with Trivy) was not done.
- **H2 console is enabled** (`spring.h2.console.enabled=true`) and is reachable on the public Railway URL. That is acceptable for this in-memory demo with no real data, but it should be disabled in a real production deployment.

---

## Architecture & Containerization

### Multi-Stage Dockerfile
To ensure a minimal image footprint and clean separation of concerns, the project uses a two-stage Docker build:

1. **Build Stage (`maven:3.9-eclipse-temurin-21`):** Resolves dependencies and compiles the application into an executable JAR (`mvn clean package -DskipTests`).
2. **Runtime Stage (`eclipse-temurin:21-jre-alpine`):** Copies solely the compiled JAR into a slim Alpine JRE image to run the service in production on port `8080`.

---

## Local Development & Testing

### Prerequisites
- Java 21 (JDK)
- Docker Desktop
- Maven (or use the included Maven Wrapper)

### Running with Maven
```bash
# Linux / macOS / WSL
./mvnw spring-boot:run

# Run the tests
./mvnw test

# Windows CMD / PowerShell
mvnw.cmd spring-boot:run
```
The application will start on `http://localhost:8080`.

### Building and Running via Docker
```bash
# Build the Docker image
docker build -t taskflow-api .

# Run container on port 8080
docker run -p 8080:8080 taskflow-api
```

---

## API Reference

| Method | Endpoint | Description | Request Body | Response |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/tasks` | Get all tasks | None | `200 OK` |
| `GET` | `/api/tasks/{id}` | Get task by ID | None | `200 OK`, or `404` if not found |
| `POST` | `/api/tasks` | Create a new task | Task JSON (`title` required) | `201 Created`, or `400` if invalid |

### Sample Payload (POST `/api/tasks`)
```json
{
  "title": "Complete Assignment 4",
  "description": "Containerize with Docker and deploy to Railway",
  "priority": "HIGH",
  "dueDate": "2026-10-20",
  "completed": false
}
```

### Testing Endpoints via cURL
```bash
# Retrieve all tasks
curl -X GET http://localhost:8080/api/tasks

# Create a new task
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Test task","description":"Testing API","priority":"HIGH","dueDate":"2026-10-20","completed":false}'
```

---

## Database Console (H2)
The in-memory H2 database console is enabled for local inspection at:
- **URL:** `http://localhost:8080/h2-console`
- **JDBC URL:** `jdbc:h2:mem:taskflowdb`
- **Username:** `sa`
- **Password:** *(empty)*
