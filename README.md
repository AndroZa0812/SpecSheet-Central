# SpecSheet Central

A full-stack electronic components trading platform for browsing, comparing, and ordering electronic parts.

## Key Features

- **Product Catalog:** Filterable and searchable catalog of electronic components.
- **Comparison Engine:** Side-by-side comparison of technical specifications.
- **Admin Panel:** Comprehensive dashboard for managing inventory, orders, and categories.
- **Profit Tracking:** Real-time margin and potential profit calculations for admins.
- **Datasheet Management:** Automatic fetching and local storage of PDF datasheets.
- **Order Lifecycle:** Full order tracking with stock level synchronization.
- **Responsive UI:** Modern, clean interface built with Svelte 5 and shadcn/ui.

## Tech Stack

- **Frontend:** Svelte 5 (Vite), Tailwind CSS, Lucide Icons
- **Backend:** Spring Boot 4.0.6, Java 21, Maven
- **Database:** PostgreSQL 15 (Production), H2 (Testing)
- **Auth:** JWT (jjwt 0.12) with Role-Based Access Control (RBAC)
- **Testing:** JUnit 5, Mockito, AssertJ, Spring Security Test

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
# Optional: Set JWT_SECRET or use the default provided in application.properties
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

### Public & User Endpoints
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/auth/register | - | Register new user |
| POST | /api/auth/login | - | Login, receive JWT |
| GET | /api/categories | - | List categories |
| GET | /api/products | - | List products (filterable) |
| GET | /api/products/{id} | - | Get product detail |
| POST | /api/orders | USER | Place order |
| GET | /api/orders/my | USER | My orders |

### Admin Endpoints (RBAC protected)
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/admin/categories | Create category |
| PUT | /api/admin/categories/{id} | Update category |
| DELETE | /api/admin/categories/{id} | Delete category |
| POST | /api/admin/products | Create product |
| PUT | /api/admin/products/{id} | Update product |
| DELETE | /api/admin/products/{id} | Delete product |
| PATCH | /api/admin/products/{id}/stock | Update stock |
| POST | /api/admin/products/{id}/datasheet | Upload/Fetch datasheet |
| GET | /api/admin/orders | All orders |
| PATCH | /api/admin/orders/{id}/status | Update order status |

## Project Structure

```
specsheet-central/
├── backend/
│   └── src/main/java/com/specsheetcentral/
│       ├── config/          # Security, DataSeeder
│       ├── controller/      # REST & Admin controllers
│       ├── dto/             # Request/response DTOs
│       ├── model/           # JPA entities with recursion fixes
│       ├── repository/      # Spring Data repos
│       ├── security/        # JWT auth filters
│       └── service/         # Business logic & File storage
├── frontend/
│   └── src/
│       ├── components/      # UI components & Modals
│       ├── lib/             # API client, types, utils
│       └── routes/          # Page components (Home, Admin, Detail, etc.)
└── docs/
    └── plans/               # Implementation & remediation plans
```

## Testing

```bash
cd backend
mvn test
```

The backend currently includes **72 automated tests** covering repositories, services, and controller integration points, ensuring robust security and business logic.
