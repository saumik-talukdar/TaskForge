# TaskForge

A multi-tenant project and task management platform inspired by real-world collaboration tools such as Jira and Trello.

TaskForge is designed around organizations, projects, memberships, role-based access control, invitation workflows, and secure authentication. The goal was not simply to build CRUD APIs, but to model realistic business rules and authorization boundaries commonly found in SaaS collaboration products.

---

## Overview

TaskForge allows organizations to manage projects and tasks while enforcing hierarchical permissions across different levels of the system.

### Hierarchy

```text
Organization
│
├── Members
│   ├── Admin
│   └── Member
│
├── Projects
│   ├── Manager
│   └── Member
│
└── Tasks
```

The platform supports:

* Organization ownership and administration
* Project management and member assignment
* Task creation, assignment, and tracking
* Invitation-based onboarding
* Secure authentication and session management

---

## Key Features

### Authentication & Security

* JWT Access Tokens
* Redis-backed Refresh Tokens
* Refresh Token Rotation
* Logout From Current Device
* Logout From All Devices
* Password Versioning for JWT Invalidation
* Email Verification
* Password Reset via Email
* BCrypt Password Hashing

### Organization Management

* Create Organizations
* Transfer Ownership
* Invite Members by Email
* Accept Organization Invitations
* Promote/Demote Members
* Remove Members
* Leave Organization
* Organization Deletion Protection Rules

### Project Management

* Create Projects
* Public and Private Project Visibility
* Project Membership Management
* Transfer Project Management
* Add/Remove Members
* Project-Level Authorization

### Task Management

* Create Tasks
* Assign Tasks
* Update Status
* Task Filtering
* Task Prioritization
* Due Dates
* Creator / Assignee / Manager Permissions

---

## Authorization Model

TaskForge implements layered authorization.

### Organization Roles

```text
Owner
└── Admin
    └── Member
```

### Project Roles

```text
Manager
└── Member
```

### Task Permissions

Actions are granted based on:

* Organization Role
* Project Role
* Task Creator
* Task Assignee

Examples:

* Organization owners cannot leave before transferring ownership.
* Project managers cannot be removed before transferring management.
* Private projects are only visible to project members and organization admins.
* Task updates are restricted by ownership and project permissions.

---

## Security Design

### Access Tokens

Short-lived JWT access tokens are used for API authentication.

### Refresh Tokens

Refresh tokens are stored in Redis and rotated on every refresh request.

```text
Refresh Token A
    ↓
Refresh Request
    ↓
Invalidate A
    ↓
Generate B
```

### Password Versioning

Each user maintains a password version.

```text
Password Change
    ↓
Version Increment
    ↓
All Existing Access Tokens Become Invalid
```

This allows global JWT invalidation without maintaining a JWT blacklist.

---

## Technology Stack

### Backend

* Java 21
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate

### Database

* PostgreSQL

### Caching

* Redis

### Authentication

* JWT
* BCrypt

### Build Tool

* Maven

### Email

* SMTP

---

## API Design

The API follows hierarchical resource routing.

Examples:

```http
/api/v1/orgs
/api/v1/orgs/{orgId}/members

/api/v1/orgs/{orgId}/projects

/api/v1/orgs/{orgId}/projects/{projectId}/members

/api/v1/orgs/{orgId}/projects/{projectId}/tasks
```

This structure mirrors the business hierarchy and simplifies authorization decisions.

---

## Project Structure

```text
domain
├── auth
├── organization
├── project
├── task
└── user

security
├── jwt
├── refresh
└── userdetails

common
├── email
├── exception
└── response
```

The project follows a domain-oriented package structure to keep business capabilities isolated and maintainable.

---

## Business Rules Implemented

* Organization owners cannot be removed.
* Organization owners cannot leave without transferring ownership.
* Project managers cannot leave while managing projects.
* Project managers cannot be removed before management transfer.
* Duplicate organization invitations are prevented.
* Invitation acceptance requires matching email ownership.
* Private projects enforce membership-based visibility.
* Organization deletion cascades safely through projects, memberships, tasks, and invitations.

---

## Future Improvements

* Activity Audit Logs
* File Attachments
* Comment System
* Real-Time Notifications
* Event-Driven Architecture
* Docker Deployment
* Integration Tests
* OpenAPI Documentation

---

## Learning Goals

This project was built to explore:

* Domain Modeling
* Role-Based Access Control (RBAC)
* Authentication & Authorization
* Multi-Tenant System Design
* Secure Session Management
* Spring Security
* Transactional Business Logic

---

## Author

Saumik Talukdar

