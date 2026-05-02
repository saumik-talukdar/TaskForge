# 🚀 TaskForge — Multi-Tenant Task Management Backend

TaskForge is a production-style backend system built with Spring Boot that supports multi-tenant task management with role-based access control, JWT authentication, and Redis-based session handling.

This project demonstrates real-world backend engineering practices including security, scalable API design, and clean architecture.

---

# 🧠 Overview

TaskForge is a **multi-tenant SaaS backend** where:

* Users belong to **Organizations**
* Organizations contain **Projects**
* Projects contain **Tasks**

```text
User → Organization → Project → Task
```

Each organization acts as a **tenant**, ensuring strict data isolation.

---

# 🔥 Features

## 🔐 Authentication & Security

* JWT-based authentication (stateless)
* Refresh token system (Redis-based)
* Token rotation & logout support
* Password versioning for token invalidation

---

## 🏢 Organization Management

* Create organization
* Join organization
* Multi-tenant isolation
* Role-based membership (ADMIN / MEMBER)

---

## 📁 Project Management

* Create project (ADMIN only)
* Update project
* Delete project
* Unique project names within organization

---

## ✅ Task Management

* Create task inside project
* Assign task to organization members
* Update task details (title/description)
* Update task status (TODO → IN_PROGRESS → DONE)
* Delete task

---

## 🛡️ Role-Based Access Control (RBAC)

### Organization Roles

| Role   | Capabilities  |
| ------ | ------------- |
| ADMIN  | Full control  |
| MEMBER | Work on tasks |

---

### Task Permissions

| Action        | ADMIN | CREATOR | ASSIGNEE |
| ------------- | ----- | ------- | -------- |
| Update task   | ✔     | ✔       | ✔        |
| Update status | ✔     | ❌       | ✔        |
| Assign task   | ✔     | ❌       | ❌        |
| Delete task   | ✔     | ✔       | ❌        |

---

## ⚡ Exception Handling

* Centralized global exception handler
* Structured API error responses
* Proper HTTP status codes

---

# 🏗️ Tech Stack

* Java 17+
* Spring Boot
* Spring Security
* Spring Data JPA
* PostgreSQL
* Redis
* Lombok

---

# 📂 Project Structure

```text
com.saumik.TaskForge
│
├── config/
├── common/
│   ├── exception/
│   └── response/
│
├── security/
│   ├── jwt/
│   ├── refresh/
│   └── userdetails/
│
├── domain/
│   ├── auth/
│   ├── user/
│   ├── organization/
│   ├── project/
│   └── task/
```

---

# 🚀 Getting Started

## 1. Clone repository

```bash
git clone https://github.com/your-username/taskforge.git
cd taskforge
```

---

## 2. Configure application.properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/taskforge
spring.datasource.username=postgres
spring.datasource.password=yourpassword

spring.jpa.hibernate.ddl-auto=update

jwt.secret=your_base64_secret
jwt.access-token-expiration=3600000
jwt.refresh-token-expiration=604800000
```

---

## 3. Run Redis

```bash
docker run -p 6379:6379 redis
```

---

## 4. Run application

```bash
./mvnw spring-boot:run
```

---

# 📌 API Demo (End-to-End Flow)

## 🔐 1. Register

```http
POST /api/v1/auth/register
```

```json
{
  "fullName": "Saumik",
  "email": "saumik@example.com",
  "password": "password123"
}
```

### Response

```json
{
  "accessToken": "jwt_access_token",
  "refreshToken": "uuid_refresh_token"
}
```

---

## 🏢 2. Create Organization

```http
POST /api/v1/orgs
Authorization: Bearer <access_token>
```

```json
{
  "name": "My Organization"
}
```

---

## 📁 3. Create Project

```http
POST /api/v1/orgs/{orgId}/projects
Authorization: Bearer <access_token>
```

```json
{
  "name": "Backend API",
  "description": "Main backend system"
}
```

---

## ✅ 4. Create Task

```http
POST /api/v1/orgs/{orgId}/projects/{projectId}/tasks
Authorization: Bearer <access_token>
```

```json
{
  "title": "Implement authentication",
  "description": "Add JWT login system"
}
```

---

## 👤 5. Assign Task

```http
PATCH /api/v1/orgs/{orgId}/projects/{projectId}/tasks/{taskId}/assign
Authorization: Bearer <access_token>
```

```json
{
  "assigneeId": "user-uuid"
}
```

---

## 🔄 6. Update Task Status

```http
PATCH /api/v1/orgs/{orgId}/projects/{projectId}/tasks/{taskId}/status
Authorization: Bearer <access_token>
```

```json
{
  "status": "IN_PROGRESS"
}
```

---

## ❌ Error Example

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Only admin can delete project",
  "timestamp": "2026-01-01T10:00:00Z"
}
```

---

# 🔄 Authentication Flow

```text
Login → Access Token
Access Token expires → Use Refresh Token
Refresh → New Access Token issued
Logout → Refresh token revoked (Redis)
```

---

# 📡 API Endpoints

## Auth

```http
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
```

## Organization

```http
POST   /api/v1/orgs
GET    /api/v1/orgs
POST   /api/v1/orgs/{id}/join
```

## Projects

```http
POST   /api/v1/orgs/{orgId}/projects
GET    /api/v1/orgs/{orgId}/projects
PATCH  /api/v1/orgs/{orgId}/projects/{projectId}
DELETE /api/v1/orgs/{orgId}/projects/{projectId}
```

## Tasks

```http
POST   /api/v1/orgs/{orgId}/projects/{projectId}/tasks
GET    /api/v1/orgs/{orgId}/projects/{projectId}/tasks
PATCH  /api/v1/orgs/{orgId}/projects/{projectId}/tasks/{taskId}
PATCH  /api/v1/orgs/{orgId}/projects/{projectId}/tasks/{taskId}/status
PATCH  /api/v1/orgs/{orgId}/projects/{projectId}/tasks/{taskId}/assign
DELETE /api/v1/orgs/{orgId}/projects/{projectId}/tasks/{taskId}
```

---

# 🧠 Key Design Decisions

* Multi-tenancy via `organization_id`
* ID-based entity relationships (no heavy JPA relations)
* Redis for refresh token storage
* Separation of authentication and business logic
* RBAC using membership table

---

# 📈 Future Improvements

* Pagination & filtering
* TenantContext (global tenant enforcement)
* Email verification
* Password reset
* Organization invite system
* Logging & monitoring

---

# 🤝 Contributing

Contributions are welcome. Feel free to open issues or submit pull requests.

---

# 📄 License

MIT License

---

# ⭐ Final Note

This project demonstrates:

* Backend architecture design
* Secure authentication system
* Multi-tenant SaaS design
* Role-based access control

If you found it useful, consider giving it a ⭐ on GitHub.
