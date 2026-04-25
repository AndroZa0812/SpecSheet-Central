# SpecSheet Central

A full-stack electronic components trading platform for browsing, comparing, and ordering electronic parts.

## Tech Stack

- **Frontend:** Svelte 5 (Vite), CSS
- **Backend:** Spring Boot 3.2, Java 21, Maven
- **Database:** PostgreSQL 15
- **Auth:** JWT (jjwt 0.12)
- **Testing:** JUnit 5, Mockito, H2, Spring Boot Test

## Prerequisites

- Java 21+ (JDK)
- Node.js 18+ with npm
- PostgreSQL 15+ (or Docker)
- Maven 3.9+

## Quick Start

### 1. Database

```bash
# Using Docker (recommended)
docker run -d --name specsheet-postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=specsheet_central \
  -p 5432:5432 postgres:15
```

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

The server starts on `http://localhost:8080`. Seed data loads automatically (6 products, 4 categories, 2 users).

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

The app is available at `http://localhost:5173`.

## Seed Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@specsheet.com | admin123 |
| User | user@specsheet.com | user123 |

## API Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/auth/register | - | Register new user |
| POST | /api/auth/login | - | Login, receive JWT |
| GET | /api/categories | - | List categories |
| POST | /api/categories | ADMIN | Create category |
| PUT | /api/categories/{id} | ADMIN | Update category |
| DELETE | /api/categories/{id} | ADMIN | Delete category |
| GET | /api/products | - | List products (filterable) |
| GET | /api/products/{id} | - | Get product detail |
| POST | /api/products | ADMIN | Create product |
| PUT | /api/products/{id} | ADMIN | Update product |
| DELETE | /api/products/{id} | ADMIN | Delete product |
| PATCH | /api/products/{id}/stock | ADMIN | Update stock |
| POST | /api/orders | USER | Place order |
| GET | /api/orders/my | USER | My orders |
| GET | /api/orders | ADMIN | All orders |
| PATCH | /api/orders/{id}/status | ADMIN | Update order status |

### Product Filtering

`GET /api/products?search=arduino&categoryId=1&manufacturer=Arduino&minPrice=10&maxPrice=100`

## Project Structure

```
specsheet-central/
├── backend/
│   └── src/main/java/com/specsheetcentral/
│       ├── config/          # Security, DataSeeder
│       ├── controller/      # REST controllers
│       ├── dto/             # Request/response DTOs
│       ├── model/           # JPA entities
│       ├── repository/      # Spring Data repos
│       ├── security/        # JWT auth
│       └── service/         # Business logic
├── frontend/
│   └── src/
│       ├── components/      # Navbar, Route, Modal
│       ├── lib/             # API client, stores, router
│       └── routes/          # Page components
└── docs/
    ├── plans/               # Implementation plan
    └── screens/             # UI mockups
```

## Testing

```bash
cd backend
mvn test
```

Runs 29 tests (repository, service, controller integration).
