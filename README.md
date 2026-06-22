# Mentorise Backend

Mentorise Backend is the server-side application for **Mentorise**, a platform that connects students with approved tutors from the same academic institution and manages the full tutoring flow: matching, messaging, lesson offers, payments, reminders, notifications, and administration.

This repository contains the backend only. It exposes REST APIs and WebSocket endpoints used by the mobile app and the admin web portal.

---

## What the Backend Supports

The backend was designed as a full application layer, not just a CRUD API. It manages authentication, authorization, business flows, data consistency, real-time communication, scheduled reminders, and integration points for external services.

Main backend capabilities:

- User registration, login, JWT-based authentication, and password reset
- Role-based access for `STUDENT`, `TUTOR`, and `ADMIN`
- Tutor application and approval workflow
- Academic matching based on majors, years, courses, and user/tutor scopes
- AI-assisted tutor recommendation reasons
- Real-time chat using WebSocket/STOMP
- Read tracking for conversations
- Session offer creation, approval, rejection, and status management
- Mock payment flow with validation and payment status handling
- Scheduled lesson reminders
- Internal notifications and Expo push notification support
- Admin APIs for users, tutors, courses, majors, notifications, and statistics
- Relational data model built with MySQL and Spring Data JPA

---

## Technology Stack

| Area | Technology |
| --- | --- |
| Language | Java |
| Framework | Spring Boot |
| Security | Spring Security, JWT, BCrypt |
| Database | MySQL |
| ORM / Persistence | Spring Data JPA |
| Real-Time Communication | WebSocket / STOMP |
| Scheduling | Spring Scheduler |
| AI Integration | Gemini API |
| Push Notifications | Expo Push API |
| File / Media Support | Cloudinary integration points |
| Build Tool | Maven |

---

## Architecture Overview

The backend follows a layered structure:

```text
Client Applications
  - Mobile App
  - Admin Web

        |
        v

API Layer
  - REST Controllers
  - WebSocket/STOMP Endpoints
  - Admin APIs

        |
        v

Business Layer
  - Services
  - Validation
  - Authorization checks
  - Matching logic
  - Payment flow
  - Notification logic
  - Reminder scheduling

        |
        v

Data Access Layer
  - Repositories
  - Spring Data JPA
  - Projections / DTO mapping

        |
        v

Database
  - MySQL
```

External services are used where needed:

- Gemini for AI-generated tutor match explanations
- Expo Push for mobile push notifications
- Cloudinary for media/document upload workflows
- Email provider for password reset messages

---

## Core Domain Modules

### Authentication and Users

The backend supports user registration and login using stateless JWT authentication. Passwords are not stored as plain text. They are stored as secure BCrypt hashes.

The system also includes password reset support with short-lived tokens, allowing both regular users and admins to reset their password securely.

### Roles and Permissions

Mentorise uses role-based authorization:

- `STUDENT` - searches for tutors, chats, receives offers, pays, and manages lessons
- `TUTOR` - manages tutor profile, chats with students, sends lesson offers, and receives reminders
- `ADMIN` - manages users, tutor approvals, academic data, notifications, and system statistics

Sensitive operations are protected using Spring Security and method-level authorization. The backend also performs ownership checks to prevent users from accessing resources that do not belong to them, such as conversations, notifications, session offers, and payments.

### Tutor Approval Flow

Users can apply to become tutors. A tutor application includes the requested teaching scopes, such as courses, years, or broader academic areas. The admin reviews the request and can approve or reject it.

When approved, the backend updates the user role and creates the relevant tutor records and approved tutor scopes. This keeps the tutor marketplace controlled and prevents unverified users from becoming tutors without review.

### Academic Scopes and Matching

The system separates between:

- what the student needs help with
- what the tutor is approved to teach

This is represented through user scopes and tutor scopes. Matching is based on academic relevance, such as major, year, and specific courses. This structure allows Mentorise to recommend tutors in a controlled and explainable way.

The backend also supports AI-generated match reasons, which help explain why a tutor is relevant for a specific student.

### Chat and Real-Time Communication

The backend includes real-time chat using WebSocket/STOMP. Connections are authenticated with JWT, and messages are routed to the correct user-specific channels.

Supported chat capabilities include:

- creating or retrieving a conversation between two users
- sending and receiving messages in real time
- conversation inbox with last-message data
- unread/read tracking
- session offer messages embedded inside chat flows
- automatic reconnect support from the client side

### Lesson Offers

Tutors can send lesson offers to students. A session offer includes time, price, status, and optional notes. Students can approve or reject an offer.

The backend manages the state of the offer and coordinates the related notification and payment flow.

Typical offer states include:

- pending
- accepted
- rejected
- completed

### Mock Payments

The current payment system is a mock implementation intended for MVP and demo purposes. It simulates a payment flow while keeping the backend structure ready for integration with a real payment provider later.

The payment module supports:

- payment creation for a session offer
- validation flow before approval
- payment statuses
- transaction references
- storage of non-sensitive payment metadata such as provider, status, card brand, and last four digits

The system is designed so a real provider such as Stripe, PayPal, or another payment gateway can be integrated later without changing the entire business flow.

> Note: This implementation is not a real payment processor and should not be used for production payment processing without integrating a certified payment provider.

### Scheduled Lesson Reminders

The backend includes a scheduled reminder mechanism for accepted lessons. It checks upcoming sessions and sends reminders at defined time points, such as:

- the evening before the lesson
- the morning of the lesson
- one hour before the lesson
- fifteen minutes before the lesson

Reminder flags are stored in the database to prevent duplicate reminders.

### Notifications and Push Tokens

The system supports internal notifications and push notifications.

Notifications can be created by system events such as:

- new message
- new session offer
- payment or offer status update
- tutor application approval or rejection
- lesson reminder
- admin broadcast

The backend also stores mobile push tokens and can deactivate invalid tokens when push delivery fails.

Admins can send notifications to:

- all users
- students
- tutors
- a specific user

### Admin Backend

The backend exposes admin-only APIs for managing the platform.

Admin capabilities include:

- user management
- tutor approval and rejection
- revoking tutor permissions
- creating additional admins
- managing majors and courses
- sending notifications
- retrieving statistics for the admin dashboard

The admin area is protected by role-based access control.

---

## Database Model

The backend uses a relational MySQL schema. The main tables are grouped by responsibility:

### Core

- `users`
- `tutors`
- `majors`
- `courses`

### Matching and Academic Scope

- `user_scopes`
- `tutor_scopes`

### Tutor Applications

- `tutor_applications`
- `tutor_application_scopes`

### Chat

- `conversations`
- `messages`
- `conversation_reads`

### Lessons and Payments

- `session_offers`
- `payments`

### Notifications and Security

- `notifications`
- `user_push_tokens`
- `password_reset_tokens`

The schema uses foreign keys, constraints, indexes, and dedicated tables for each main process. This keeps the data model maintainable and ready for future extensions such as ratings, video sessions, real payment settlement, and more advanced AI recommendations.

---

## Security Highlights

Security is built into the backend through several layers:

- Spring Security for authentication and authorization
- Stateless JWT authentication
- BCrypt password hashing
- Role-based access control
- Method-level security for sensitive operations
- JWT authentication for WebSocket connections
- Ownership checks to reduce IDOR risks
- Short-lived password reset tokens
- Parameterized database access through Spring Data JPA
- Global exception handling without exposing stack traces
- Environment variables for secrets and service credentials

Sensitive configuration such as database credentials, JWT secret, mail credentials, Gemini key, and external service settings should be stored outside the repository.

---

## API Areas

The backend is organized around the following API areas:

- Authentication
- User profile
- Tutor profile
- Tutor applications
- Matching / discovery
- Conversations and messages
- Session offers
- Payments
- Notifications
- Push tokens
- Admin users
- Admin tutor applications
- Admin courses and majors
- Admin statistics

Exact endpoints may vary by implementation and can be reviewed inside the controller classes.

---

## Running Locally

### Prerequisites

- Java 17+
- Maven
- MySQL
- Required environment variables configured

### Clone

```bash
git clone <repository-url>
cd <backend-folder>
```

### Configure Database

Create a MySQL database for the project and configure the connection in your local environment or application configuration.

Example values:

```env
DB_URL=jdbc:mysql://localhost:3306/mentorise
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
GEMINI_API_KEY=your_gemini_key
MAIL_USERNAME=your_mail_user
MAIL_PASSWORD=your_mail_password
```

Use the exact variable names defined in the project configuration files.

### Run

Linux / macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

The backend should start on the configured server port.

---

## Development Notes

- Keep `.env` and local configuration files out of Git.
- Do not commit real credentials, tokens, API keys, or database passwords.
- The mock payment flow is for testing and demonstration only.
- Admin APIs should always remain protected by admin-only authorization.
- WebSocket connections must be authenticated before subscriptions are accepted.
- Database changes should preserve foreign key consistency and existing business flows.

---

## Future Extensions

The current backend structure was built to support future expansion, including:

- real payment gateway integration
- Zoom API or another video meeting provider
- tutor rating and review flow
- advanced AI-based matching
- tutor earnings backend settlement
- richer admin reporting
- production deployment hardening

---

## Summary

Mentorise Backend provides the server-side foundation for a complete tutoring platform. It combines authentication, permissions, academic matching, real-time communication, payments-ready lesson flow, notifications, scheduling, and admin management in a modular Spring Boot application.

The backend is designed to support the full process from student discovery to an approved lesson, while remaining maintainable and ready for future production-level integrations.
