# Gestion_projet – Film Management (Backend: Spring Boot, Frontend: Vue 3)

A full-stack project for managing films, episodes, scenes, sequences, and users. The backend is a Java 17 Spring Boot application using MySQL and Spring Security; the frontend is a Vue 3 app built with Vite.

Note: Some details are inferred from the codebase. Where information is missing, TODOs are added for maintainers to fill in.

## Table of Contents
- Overview
- Tech Stack
- Requirements
- Project Structure
- Setup
  - Database
  - Backend (Spring Boot)
  - Frontend (Vue 3 + Vite)
- Run Commands and Scripts
- Configuration and Environment Variables
- API & Entry Points
- Data and Demo Users
- Tests
- Troubleshooting
- License

## Overview
This repository contains:
- A Spring Boot backend exposing REST endpoints under the /api context path, persisting to a MySQL database.
- A Vue 3 frontend served via Vite for local development (default on http://localhost:5173) consuming the backend API.

Primary goal: manage film-related entities and users. Security is currently permissive in code (Spring Security configured to permit all requests), suitable for development.

## Tech Stack
- Backend:
  - Java 17
  - Spring Boot 3.1 (web, data-jpa, thymeleaf, security, devtools)
  - MySQL (mysql-connector-java 8.0.33)
  - Maven
  - Lombok (provided)
- Frontend:
  - Vue 3
  - Vite
  - Pinia, Vue Router, Vuetify, Axios, Tiptap/Quill
- Database: MySQL 8+

## Requirements
- Java 17 (JDK)
- Maven 3.8+
- Node.js 18+ and npm (for frontend)
- MySQL Server 8+

## Project Structure
```
Gestion_projet/
├─ backend/                      # Spring Boot application (Maven)
│  ├─ src/main/java/com/example/films/
│  │  ├─ FilmManagementApplication.java  # Backend entry point
│  │  └─ config/                 # Security and Web/CORS configuration
│  ├─ src/main/resources/
│  │  └─ application.properties  # App configuration (DB, CORS, context path)
│  └─ pom.xml
├─ frontend/                     # Vue 3 + Vite application
│  ├─ package.json               # npm scripts (dev, build, preview)
│  └─ src/
├─ Base de données/              # SQL scripts and credentials notes
│  └─ mysql/
│     ├─ BDD_mysql.sql           # TODO: confirm main schema file name/path
│     └─ create_test_user.sql
├─ assets/                       # Project assets
└─ README.md                     # This file
```

## Setup
### 1) Database (MySQL)
1. Create a MySQL database named gestion_projet_film (as per application.properties).
2. Create a database user with privileges (a helper script exists):
   - File: Base de données/mysql/create_test_user.sql
3. Initialize schema/data using the SQL scripts in Base de données/ or Base de données/mysql/ (e.g., BDD_mysql.sql, BDD.sql).
   - TODO: Confirm the canonical schema/init script and update this section with the exact file to run and order of execution.

Default backend DB configuration (backend/src/main/resources/application.properties):
```
spring.datasource.url=jdbc:mysql://localhost:3306/gestion_projet_film
spring.datasource.username=gestion_projet_film_user
spring.datasource.password=test1234Welcome@
spring.jpa.hibernate.ddl-auto=validate
```
You can override these via environment variables (see Configuration and Environment Variables).

### 2) Backend (Spring Boot)
From the backend directory:
- Build: mvn clean package
- Run (dev): mvn spring-boot:run
- Port and context path: by default http://localhost:8080/api (server.servlet.context-path=/api)
- CORS: allowed origin http://localhost:5173 (see WebConfig) and permissive pattern in SecurityConfig for development.

### 3) Frontend (Vue 3 + Vite)
From the frontend directory:
- Install dependencies: npm install
- Run dev server: npm run dev (defaults to http://localhost:5173)
- Build for production: npm run build
- Preview build locally: npm run preview

## Run Commands and Scripts
- Backend (from backend/):
  - mvn spring-boot:run — start backend in development mode
  - mvn test — run backend tests (if any)
  - mvn clean package — build JAR
- Frontend (from frontend/):
  - npm run dev — start Vite dev server on port 5173
  - npm run build — build frontend
  - npm run preview — preview built frontend
- Database:
  - Base de données/mysql/create_test_user.sql — helper script to create a DB user (execute in MySQL)
  - Base de données/mysql/BDD_mysql.sql — TODO: confirm schema and run instructions

## Configuration and Environment Variables
Spring Boot supports overriding application.properties via environment variables.
Common overrides:
- SPRING_DATASOURCE_URL (default jdbc:mysql://localhost:3306/gestion_projet_film)
- SPRING_DATASOURCE_USERNAME (default gestion_projet_film_user)
- SPRING_DATASOURCE_PASSWORD (default test1234Welcome@)
- SERVER_SERVLET_CONTEXT_PATH (default /api)
- SPRING_JPA_HIBERNATE_DDL_AUTO (default validate)
- SPRING_MVC_CORS_ALLOWED_ORIGINS (default http://localhost:5173 per properties; WebConfig allows same)

Frontend configuration typically uses Vite env files (.env, .env.local) with VITE_*-prefixed variables.
- TODO: Document frontend env variables if used (e.g., VITE_API_BASE_URL). Search code and update here.

## API & Entry Points
- Backend entry point: com.example.films.FilmManagementApplication (Spring Boot)
- REST base path: http://localhost:8080/api
- Security: Current SecurityConfig permits all requests (development). Adjust before production.
- TODO: Document key REST endpoints (controllers) once stabilized.

## Data and Demo Users
Reference credentials and roles are listed in:
- Base de données/Login et MDP.txt
Example entries include ADMIN and SCENARISTE users. Use these for development/testing if seeded in the DB.
- TODO: Confirm whether these users are auto-seeded and provide actual login flows/endpoints.

## Tests
- Backend: spring-boot-starter-test is included. Run mvn test in backend/.
- Frontend: No explicit test setup found. TODO: Add unit/e2e tests and document run commands here (e.g., Vitest/Cypress).

## Troubleshooting
- Cannot connect to DB: Ensure MySQL is running, DB exists, and credentials match application.properties or environment overrides.
- CORS issues: Backend allows origin http://localhost:5173. Ensure frontend dev server runs on that port or update allowed origins.
- Port conflicts: Change frontend dev server port via Vite config or backend server port via SERVER_PORT env var.

## License
TODO: Add a LICENSE file and specify the project’s license.
