
# Doctor & Scheduling Service

Microservice handling doctor management and slot scheduling for HMS.

### Run locally
```bash
mvn clean package
docker compose up --build
```
Service URL: http://localhost:8082

### APIs
- POST /v1/doctors
- GET /v1/doctors/{id}
- PUT /v1/doctors/{id}
- DELETE /v1/doctors/{id}
- GET /v1/doctors?department=...
- POST /v1/doctors/{id}/availability
- POST /v1/doctors/{id}/reserve
- POST /v1/doctors/{id}/reserve/{holdId}/confirm
- POST /v1/doctors/{id}/reserve/{holdId}/release
