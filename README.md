# 🎓 Smart Student Management System

> A full-stack academic management platform built with **Java, Spring Boot, Spring Security, JWT, PostgreSQL, HTML, CSS, and JavaScript**.

🌐 **Live Demo:**  
https://student-management-system-dbc6.onrender.com
---
## 🔐 Demo Login Credentials

The application provides three role-based demo accounts:

| Role | Username | Password |
|---|---|---|
| 👨‍💼 Admin | `admin` | `Admin@123` |
| 👨‍🏫 Teacher | `teacher1` | `Teacher@123` |
| 🎓 Student | `student1` | `Student@123` |

> **Note:** These are demo/development credentials created by the application's sample-data initializer. Do not use these credentials in a production environment.


## 📖 Project Overview

A full-stack Java Spring Boot application for managing students, teachers, departments,
subjects, exams, marks, attendance, and rule-based student performance analysis —
built with role-based authentication (Admin / Teacher / Student) and a vanilla
HTML/CSS/JavaScript frontend.

> **Note on how this project was built:** development happened in phases inside one
> conversation. Phase 1–2 (Student entity, CRUD, validation, pagination, search/filter,
> exception handling) were built first and are fully preserved. Phases 3+ (Department,
> Teacher, Subject, Auth/JWT, Marks, Attendance, Results, Performance analysis,
> Dashboards, and the full frontend) were added on top of that same codebase in this
> pass. Every Phase 1–2 API still works exactly as before, with one intentional change:
> `Student.department` moved from a plain string to a proper `Department` foreign key,
> as the full spec explicitly requires ("do not store department name as plain text if
> it should be a relationship"). `StudentRequestDTO`/`StudentResponseDTO` now use
> `departmentId` / `departmentName` instead of a free-text `department` field.


### 🚀 Highlights

- 🔐 JWT-based authentication & role-based authorization
- 👨‍💼 Admin, 👨‍🏫 Teacher & 🎓 Student dashboards
- 👨‍🎓 Student and teacher management
- 🏢 Department and subject management
- 📝 Examination and marks management
- 📅 Attendance tracking
- 📊 Results and student performance analysis
- 🔒 BCrypt password encryption
- 🗄️ PostgreSQL database
- ☁️ Deployed on Render

### 🛠️ Tech Stack

**Backend:** Java • Spring Boot • Spring Security • JPA • Hibernate  
**Frontend:** HTML • CSS • JavaScript  
**Database:** PostgreSQL  
**Authentication:** JWT • BCrypt  
**Build Tool:** Maven  
**Deployment:** Render

---

## 📸 Screenshots

### 🔑 Login Page

![Login Page](screenshots/login.png)

### 👨‍💼 Admin Dashboard

![Admin Dashboard](screenshots/admin-dashboard.png)

### 👨‍🏫 Teacher Dashboard

![Teacher Dashboard](screenshots/teacher-dashboard.png)

### 🎓 Student Dashboard

![Student Dashboard](screenshots/student-dashboard.png)

### 👨‍🎓 Student Management

![Student Management](screenshots/students.png)

### 📊 Student Performance

![Student Performance](screenshots/performance.png)

### 📅 Attendance Management

![Attendance Management](screenshots/attendance.png)


## Features

- **Authentication & Authorization** — JWT-based login, BCrypt password hashing,
  stateless sessions, role-based access control (`ADMIN`, `TEACHER`, `STUDENT`) enforced
  with Spring Security `@PreAuthorize` on every endpoint.
- **Student Management** — full CRUD, validation, pagination, search, filtering by
  department/year/status, linked login account.
- **Department / Teacher / Subject Management** — full CRUD with proper JPA
  relationships (`@ManyToOne`/`@OneToOne`), search, pagination.
- **Exam Management** — INTERNAL / MIDTERM / FINAL / PRACTICAL exam types.
- **Marks Management** — enter/update marks per student/subject/exam, automatic
  percentage/grade/pass-fail calculation (configurable grading table, not hardcoded
  in controllers).
- **Result Management** — aggregates a student's marks into total marks, overall
  percentage, grade, an approximate GPA, and a list of failed subjects.
- **Attendance Management** — record present/absent per student/subject/date,
  automatic percentage calculation, "Attendance Warning" flag below 75%.
- **Smart Performance Analysis** — a transparent, **rule-based** (not machine-learning)
  engine that classifies each student as `GOOD`, `NEEDS_ATTENTION`, or `AT_RISK` based
  on CGPA, attendance %, and failed-subject count, plus a blended 0–100 performance
  score.
- **Dashboards** — separate Admin, Teacher, and Student dashboards with real aggregated
  data.
- **DTO layer throughout** — no JPA entity is ever returned directly from a controller,
  avoiding circular JSON and leaking internal fields (e.g. password hashes).
- **Global exception handling** — consistent JSON error shape across the whole API.
- **Sample data** — an admin, a teacher, and a student account (with linked
  department/subject/marks/attendance records) are seeded automatically on first run.

---

## Technology Stack

| Layer      | Technology                                                             |
|------------|-------------------------------------------------------------------------|
| Backend    | Java 25, Spring Boot 4.1.0, Spring MVC, Spring Data JPA, Hibernate      |
| Security   | Spring Security, JWT (jjwt 0.12.x), BCrypt                             |
| Database   | MySQL 8                                                                 |
| Frontend   | HTML5, CSS3, vanilla JavaScript, Fetch API (no frameworks)              |
| Build      | Maven                                                                   |
| Testing    | JUnit 5, Mockito, AssertJ                                               |

---

## Project Structure

```
src/main/java/com/studentmanagement/
├── config/         DataInitializer (seeds sample data on first run)
├── controller/      REST controllers (thin - delegate to services)
├── dto/
│   ├── request/     Input DTOs, validated with Jakarta Validation
│   └── response/     Output DTOs (never expose entities directly)
├── entity/           JPA entities
├── exception/         Custom exceptions + @RestControllerAdvice
├── repository/       Spring Data JPA repositories
├── security/         JWT filter, JwtUtil, SecurityConfig, UserDetailsService
├── service/           Business logic (grading, attendance %, performance rules)
└── util/               GradeCalculator (single source of truth for grade boundaries)

src/main/resources/
├── static/            Full HTML/CSS/JS frontend (served directly by Spring Boot)
└── application.properties

src/test/java/com/studentmanagement/
├── util/GradeCalculatorTest.java
└── service/            StudentServiceTest, AuthServiceTest, AttendanceServiceTest
```

---

## Database Design

Core entities and relationships:

- `User` (username, email, password hash, role, enabled) — the login account.
- `Student` `@ManyToOne` → `Department`, optional `@OneToOne` → `User`.
- `Teacher` `@ManyToOne` → `Department`, optional `@OneToOne` → `User`.
- `Department` `@OneToMany` → `Student`, `Teacher`, `Subject` (inverse side).
- `Subject` `@ManyToOne` → `Department`, `@ManyToOne` → `Teacher` (nullable).
- `Exam` — standalone (name, type, date, semester, academic year).
- `Marks` `@ManyToOne` → `Student`, `Subject`, `Exam` (one row per student/subject/exam
  combination — enforced in the service layer).
- `Attendance` `@ManyToOne` → `Student`, `Subject`, with a `date` + `PRESENT`/`ABSENT`
  status.

`spring.jpa.hibernate.ddl-auto=update` is used, so schema changes apply incrementally
and existing data is preserved between restarts.

> **Simplification:** "Semester" is stored as a plain `Integer` (1–8) on `Student`,
> `Subject`, and `Exam`, rather than as its own linked entity. A dedicated `Semester`
> CRUD entity was in the original spec but would have meant refactoring three foreign
> keys for limited practical benefit at this project's scale — noted here as a
> deliberate simplification, not an oversight.

---

## API Endpoints

All endpoints are prefixed with `/api`. Endpoints marked 🔒 require a valid JWT;
role restrictions are noted in parentheses.

### Auth
| Method | Endpoint             | Notes                                |
|--------|-----------------------|----------------------------------------|
| POST   | `/auth/login`          | Returns `{ token, username, role }`   |
| POST   | `/auth/register`       | Creates a bare login account          |

### Students
| Method | Endpoint                  | Access                          |
|--------|-----------------------------|----------------------------------|
| POST   | `/students`                  | 🔒 ADMIN                         |
| GET    | `/students/{id}`             | 🔒 ADMIN/TEACHER, or STUDENT (self only) |
| GET    | `/students/me`               | 🔒 STUDENT                       |
| GET    | `/students`                  | 🔒 ADMIN/TEACHER (paginated)     |
| PUT    | `/students/{id}`             | 🔒 ADMIN                         |
| DELETE | `/students/{id}`             | 🔒 ADMIN                         |
| GET    | `/students/search?keyword=`  | 🔒 ADMIN/TEACHER                 |
| GET    | `/students/filter?departmentId=&year=&status=` | 🔒 ADMIN/TEACHER |

### Departments / Teachers / Subjects / Exams
Same CRUD + search/filter + pagination shape, e.g.:
`/departments`, `/teachers`, `/subjects`, `/exams` (+ `/{id}`, `/search`, `/filter`).
Create/Update/Delete require ADMIN (Exams also allow TEACHER to create/update).

### Marks
| Method | Endpoint                        | Access             |
|--------|-----------------------------------|----------------------|
| POST   | `/marks`                          | 🔒 ADMIN/TEACHER     |
| PUT    | `/marks/{id}`                     | 🔒 ADMIN/TEACHER     |
| DELETE | `/marks/{id}`                     | 🔒 ADMIN             |
| GET    | `/marks/student/{studentId}`      | 🔒 ADMIN/TEACHER, or STUDENT (self) |
| GET    | `/marks/subject/{subjectId}`      | 🔒 ADMIN/TEACHER     |
| GET    | `/marks/exam/{examId}`            | 🔒 ADMIN/TEACHER     |

### Attendance
| Method | Endpoint                                | Access             |
|--------|--------------------------------------------|----------------------|
| POST   | `/attendance`                              | 🔒 ADMIN/TEACHER     |
| PUT    | `/attendance/{id}`                         | 🔒 ADMIN/TEACHER     |
| DELETE | `/attendance/{id}`                         | 🔒 ADMIN             |
| GET    | `/attendance/student/{studentId}`          | 🔒 ADMIN/TEACHER, or STUDENT (self) |
| GET    | `/attendance/student/{studentId}/summary`  | 🔒 ADMIN/TEACHER, or STUDENT (self) |
| GET    | `/attendance/subject/{subjectId}`          | 🔒 ADMIN/TEACHER     |

### Results & Performance
| Method | Endpoint                                    | Access             |
|--------|------------------------------------------------|----------------------|
| GET    | `/results/student/{studentId}`                 | 🔒 ADMIN/TEACHER, or STUDENT (self) |
| GET    | `/results/student/{studentId}/performance`     | 🔒 ADMIN/TEACHER, or STUDENT (self) |

### Dashboards
| Method | Endpoint             | Access       |
|--------|-----------------------|----------------|
| GET    | `/dashboard/admin`     | 🔒 ADMIN       |
| GET    | `/dashboard/teacher`   | 🔒 TEACHER     |
| GET    | `/dashboard/student`   | 🔒 STUDENT     |

### Users (admin-only account management)
`GET/POST /users`, `GET /users/{id}`, `GET /users/search`, `PATCH /users/{id}/enabled`,
`DELETE /users/{id}` — all 🔒 ADMIN.

Every error response follows the same shape:
```json
{
  "timestamp": "2026-08-15T10:15:30",
  "status": 404,
  "message": "Student not found with id: 10",
  "path": "/api/students/10"
}
```

---

## Setup & Running

### Prerequisites
- Java 25 JDK
- Maven (or use the included `./mvnw` wrapper)
- MySQL 8 running locally

### 1. Create the database
```sql
CREATE DATABASE student_management;
```
No tables need to be created manually — `ddl-auto=update` handles that on first run.

### 2. Configure the connection
Defaults live in `src/main/resources/application.properties` and read from environment
variables, so **no secret is hardcoded**:

| Property               | Environment variable   | Default (dev only)        |
|-------------------------|--------------------------|------------------------------|
| DB username              | `DB_USERNAME`             | `root`                        |
| DB password               | `DB_PASSWORD`             | `root`                        |
| JWT signing secret        | `JWT_SECRET`              | a dev-only placeholder string |
| JWT expiry (ms)            | `JWT_EXPIRATION_MS`        | `86400000` (24h)              |
| Seed sample data on boot    | `SAMPLE_DATA_ENABLED`       | `true`                          |

Set your own DB credentials (and a real `JWT_SECRET` if deploying anywhere beyond your
own machine) either as environment variables or by editing `application.properties`
directly.

### 3. Run the application
```bash
./mvnw spring-boot:run
```
or from Eclipse: right-click the project → **Run As → Spring Boot App**.

The app starts on `http://localhost:8080`. On first run (empty `user` table) it seeds
sample data automatically — this never re-runs or duplicates data on subsequent
restarts.

### 4. Open the frontend
Go to **http://localhost:8080/** — it redirects to the login page automatically.

### 5. Test credentials (seeded automatically)
| Role    | Username   | Password     |
|---------|-------------|----------------|
| ADMIN   | `admin`      | `Admin@123`     |
| TEACHER | `teacher1`   | `Teacher@123`   |
| STUDENT | `student1`   | `Student@123`   |

---

## Testing the Application

### Automated tests (no database needed)
```bash
./mvnw test
```
This runs `GradeCalculatorTest`, `StudentServiceTest`, `AuthServiceTest`,
`AttendanceServiceTest`, `MarksServiceTest`, and `PerformanceServiceTest` — all pure
Mockito/JUnit tests with no Spring context or database dependency, covering student
CRUD, login, grade/percentage/pass-fail calculation, attendance percentage/at-risk
logic, and the rule-based performance classification (GOOD/NEEDS_ATTENTION/AT_RISK).

> `StudentManagementSystemApplicationTests` (the original context-load test) uses
> `@SpringBootTest`, which needs a live MySQL connection to pass — that one only runs
> successfully with the database from step 1 available.

### Manual testing via the UI
1. Log in as `admin` → check the Admin Dashboard shows real counts.
2. Go to **Students** → add a student, edit it, search for it, delete it.
3. Go to **Departments/Teachers/Subjects/Exams** → same CRUD flow.
4. Go to **Marks** → select the seeded subject, click Load, add a new marks entry.
5. Go to **Attendance** → select a student, click Load, see the attendance % and
   warning badge if under 75%.
6. Go to **Results** → pick a student, see the aggregated result and performance
   analysis (GOOD / NEEDS_ATTENTION / AT_RISK with reasons).
7. Log out, log in as `teacher1` → check the Teacher Dashboard and that admin-only
   buttons (Add/Delete) are hidden.
8. Log out, log in as `student1` → check the Student Dashboard, and that trying to view
   `/api/students/2` (a different student's ID) via a raw API call returns `403`.

### Manual testing via Postman
1. `POST /api/auth/login` with `{"username":"admin","password":"Admin@123"}` → copy the
   `token`.
2. Add header `Authorization: Bearer <token>` to subsequent requests.
3. Exercise any endpoint from the table above.

---

## Important Features Implemented
- JWT auth with role-based `@PreAuthorize` on every protected endpoint.
- A student can never view another student's marks/attendance/results/profile, even by
  changing the ID in the URL — enforced server-side, not just hidden in the UI.
- Grading logic lives in one place (`GradeCalculator`), not duplicated across
  controllers.
- Performance analysis is explicitly rule-based and labeled as such in both the API
  response's own field naming and the UI copy — never described as machine learning.
- Sample data seeding is idempotent (checked via `userRepository.count() == 0`).

## Limitations / Not Implemented
- No dedicated `Semester` entity/CRUD (see Database Design note above) — semester is a
  plain integer field.
- No `semesters.html`, `student-details.html`, or `profile.html` pages — student detail
  is covered inline in the Students table/modal, and profile info is shown on each
  role's dashboard instead of a separate page.
- The "pending marks" figure on the Teacher Dashboard is an approximation (students in
  the subject's department with no marks row yet for that subject/any exam), not a
  precise per-exam pending count.
- Admin dashboard's at-risk count and average-attendance figures are computed by
  iterating all students on each request — fine at typical college-project scale, but
  would need caching/batching for a much larger dataset.
- No password-reset or refresh-token flow — tokens simply expire after 24h and require
  re-login.
- No CI pipeline or Dockerfile included.

## Manual Steps You May Need
- Create the `student_management` database (step 1 above) — the app does not create
  the database itself, only tables/columns within it.
- If you already have Student rows from the Phase 2 version of this project without a
  `department_id`, `student_id`, or `email`, you'll need to either clear that table or
  backfill those columns manually — Hibernate's `ddl-auto=update` adds new
  columns/constraints but won't populate them for you.
- Set a real `JWT_SECRET` environment variable before deploying anywhere other than
  your own machine; the default in `application.properties` is explicitly for local
  development only.

## Future Enhancements
- Refresh tokens / "remember me" longer-lived sessions.
- A dedicated Semester entity if the school genuinely tracks non-integer semester
  metadata (e.g. start/end dates, active semester flag).
- Server-side caching for dashboard aggregates.
- CSV/PDF export for results and attendance reports.
- Email notifications when a student is flagged AT_RISK.
