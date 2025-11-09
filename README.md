# 🏥 HMS Doctor Service

The **Doctor Service** is a standalone Spring Boot microservice in the Hospital Management System (HMS).
It manages doctor profiles, scheduling, reservations, and appointment confirmations.
It provides REST APIs for doctor management and availability handling, integrated with MySQL and Docker.

This service is containerized and automatically built & published to **GitHub Container Registry (GHCR)** via CI/CD.

---

## 🚀 Features

* CRUD operations for doctor management
* Department-wise filtering and pagination
* Reservation and confirmation of appointment slots
* Availability management
* Soft delete (deactivation) for doctors
* Validation and role-based access via headers
* Health check endpoints for Kubernetes (`/healthcheck/live`, `/healthcheck/ready`)
* OpenAPI 3.0 documentation (Swagger UI)
* MySQL integration with Flyway migrations
* Dockerfile and Docker Compose for local setup
* GitHub Actions pipeline for CI/CD and GHCR publishing

---

## 🧩 Tech Stack

| Layer            | Technology                     |
| ---------------- | ------------------------------ |
| Language         | Java 17                        |
| Framework        | Spring Boot 3.x                |
| ORM              | Spring Data JPA                |
| Database         | MySQL 8                        |
| Migrations       | Flyway                         |
| Build Tool       | Maven                          |
| API Docs         | Springdoc OpenAPI (Swagger UI) |
| CI/CD            | GitHub Actions                 |
| Containerization | Docker & GHCR                  |

---

## ⚙️ Local Setup

### 🧰 Prerequisites

* Java 17+
* Maven 3.6+
* Docker & Docker Compose

---

### 1️⃣ Build the project

```bash
mvn clean package
```

---

### 2️⃣ Run locally with Docker Compose

```bash
docker compose up --build
```

Service will start at:
👉 **[http://localhost:8082](http://localhost:8082)**

---

### 3️⃣ Stop containers

```bash
docker compose down
```

---

## 🗃️ Database Configuration

| Property                        | Default Value                                                                                        |
| ------------------------------- | ---------------------------------------------------------------------------------------------------- |
| `spring.datasource.url`         | `jdbc:mysql://127.0.0.1:33062/doctordb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `spring.datasource.username`    | `root`                                                                                               |
| `spring.datasource.password`    | `example`                                                                                            |
| `spring.jpa.hibernate.ddl-auto` | `update`                                                                                             |

---

## 🧠 API Documentation

* Swagger UI → [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)
* OpenAPI JSON → [http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs)

---

## 📘 Available APIs

### 1️⃣ Create Doctor

**POST** `/v1/doctors`

Creates a new doctor record.
Requires `X-User-Role` header.

**Request Body:**

```json
{
  "name": "Dr. Kalyani",
  "email": "kalyani@example.com",
  "phone": "+919900112233",
  "department": "Cardiology",
  "specialization": "Heart Surgery",
  "dailyCapacity": 10
}
```

**Response:**
✅ `200 OK`

```json
{
  "doctorId": 1,
  "name": "Dr. Kalyani",
  "department": "Cardiology",
  "specialization": "Heart Surgery"
}
```

---

### 2️⃣ Get All Doctors (Paginated)

**GET** `/v1/doctors?page=0&size=10&department=Cardiology`

Returns a paginated list of doctors, optionally filtered by department.

**Response:**

```json
{
  "content": [
    {
      "doctorId": 1,
      "name": "Dr. Kalyani",
      "email": "kalyani@example.com",
      "department": "Cardiology"
    }
  ],
  "totalPages": 1,
  "totalElements": 1
}
```

---

### 3️⃣ Get Doctor by ID

**GET** `/v1/doctors/{id}`

Fetches details of a specific doctor.

**Response:**
✅ `200 OK`
❌ `404 Not Found`

---

### 4️⃣ Update Doctor

**PUT** `/v1/doctors/{id}`
Requires header `X-User-Role`.

**Request Body:**

```json
{
  "name": "Dr. Kalyani M",
  "email": "kalyani.m@example.com",
  "department": "Cardiology"
}
```

---

### 5️⃣ Delete (Deactivate) Doctor

**DELETE** `/v1/doctors/{id}`
Requires header `X-User-Role`.

Soft deletes (marks inactive) a doctor record.
✅ `200 OK`
❌ `404 Not Found`

---

### 6️⃣ Reserve Slot

**POST** `/v1/doctors/{id}/reserve`
Reserves a slot temporarily for a patient.

**Request Body:**

```json
{
  "slotStart": "2025-11-09T10:00:00",
  "slotEnd": "2025-11-09T10:30:00",
  "appointmentId": 123,
  "ttlMinutes": 15
}
```

---

### 7️⃣ Release Slot

**POST** `/v1/doctors/{id}/reserve/{holdId}/release`
Releases a previously reserved slot.

---

### 8️⃣ Confirm Appointment

**POST** `/v1/doctors/{id}/reserve/{holdId}/confirm`
Confirms a reserved slot for a patient.

**Request Body:**

```json
{
  "appointmentId": 123
}
```

---

### 9️⃣ Update Availability

**POST** `/v1/doctors/{id}/availability`
Updates doctor’s availability slots.

**Request Body:**

```json
{
  "slotStart": "2025-11-09T09:00:00",
  "slotEnd": "2025-11-09T17:00:00",
  "patientId": 10
}
```

---

### 🔍 Health Check

| Endpoint             | Description                                 |
| -------------------- | ------------------------------------------- |
| `/healthcheck/live`  | Returns if the service is live              |
| `/healthcheck/ready` | Returns if the service is ready for traffic |

---

## ⚠️ Error Handling

| Exception                   | Status | Description                    |
| --------------------------- | ------ | ------------------------------ |
| `BadRequestException`       | 400    | Missing or invalid input       |
| `ResourceNotFoundException` | 404    | Doctor not found               |
| `AccessDeniedException`     | 403    | Invalid or missing role header |

**Example Error:**

```json
{
  "code": "BAD_REQUEST",
  "message": "email and phone are required",
  "timestamp": "2025-11-09T07:15:45Z"
}
```

---

## 🧪 Testing

### Unit Tests

```bash
mvn test
```

### Manual Testing

* Swagger UI → [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)
* OpenAPI JSON → [http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs)

---

## 🧰 CI/CD – GitHub Actions

The pipeline:

* Builds the project
* Runs unit tests
* Builds Docker image
* Pushes image to GHCR

**Image name:**
`ghcr.io/<github-username>/hms-doctor:latest`

To make the package public:

1. Go to GitHub → Profile → Packages
2. Select the package → **Settings**
3. Change visibility → **Public**

---

## 🗃️ Database Schema

| Column           | Type         | Description            |
| ---------------- | ------------ | ---------------------- |
| `doctor_id`      | BIGINT (PK)  | Primary key            |
| `name`           | VARCHAR(200) | Doctor’s full name     |
| `email`          | VARCHAR(200) | Email address          |
| `phone`          | VARCHAR(50)  | Contact number         |
| `department`     | VARCHAR(100) | Department name        |
| `specialization` | VARCHAR(100) | Specialization         |
| `daily_capacity` | INT          | Max daily appointments |
| `active`         | BOOLEAN      | Active flag            |
| `created_at`     | TIMESTAMP    | Record creation time   |

---

## 📦 Project Structure

```
src/main/java/org/hms/doctor/
 ├── controller/
 │    ├── DoctorController.java
 │    ├── SchedulingController.java
 │    └── HealthCheckController.java
 ├── service/
 │    └── DoctorService.java
 ├── repository/
 │    └── DoctorRepository.java
 ├── model/
 │    └── Doctor.java
 ├── dto/
 │    ├── ReserveRequest.java
 │    ├── ConfirmRequest.java
 │    └── AvailabilityRequest.java
 ├── exception/
 │    ├── BadRequestException.java
 │    └── ResourceNotFoundException.java
 └── DoctorServiceApplication.java

src/main/resources/
 ├── application.yml
 ├── db/migration/V1__init_doctor.sql
 └── data.sql
```

---

## 🔗 Useful URLs

| Type         | URL                                                                                        |
| ------------ | ------------------------------------------------------------------------------------------ |
| Application  | [http://localhost:8082](http://localhost:8082)                                             |
| Swagger UI   | [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html) |
| OpenAPI JSON | [http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs)                     |
| Health Check | [http://localhost:8082/healthcheck/live](http://localhost:8082/healthcheck/live)           |

---

## 👥 Maintainers

**HMS Development Team**
Developed as part of the Hospital Management System (HMS) microservices project.

✅ **Status:** Functional, containerized, and CI/CD ready for deployment to GHCR.

---
