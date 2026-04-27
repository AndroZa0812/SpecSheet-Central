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
- **Database:** PostgreSQL 15
- **Auth:** JWT (jjwt 0.12) with Role-Based Access Control (RBAC)
- **Testing:** JUnit 5, Mockito, AssertJ, Spring Security Test

---

## Prerequisites

- **Java 21+** (JDK) — [Download](https://adoptium.net/)
- **Node.js 18+** with npm — [Download](https://nodejs.org/)
- **PostgreSQL 15+** (or Docker)
- **Maven 3.9+** — [Download](https://maven.apache.org/download.cgi)

---

## Running from Scratch

### 1. PostgreSQL Setup

#### Option A: Docker (Recommended)

```bash
docker run -d --name specsheet-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=specsheet_central \
  -p 5432:5432 \
  postgres:15
```

#### Option B: Native Installation

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo -u postgres psql -c "CREATE DATABASE specsheet_central;"
sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'postgres';"
```

**macOS (Homebrew):**
```bash
brew install postgresql@15
brew services start postgresql@15
createdb specsheet_central
```

**Windows:**
1. Download installer from [postgresql.org](https://www.postgresql.org/download/windows/)
2. Run installer, set password to `postgres`
3. Open pgAdmin or psql and create database `specsheet_central`

> **Default connection:** `jdbc:postgresql://localhost:5432/specsheet_central` with username `postgres` / password `postgres`.
> 
> To use different credentials, edit `backend/src/main/resources/application.properties`.

---

### 2. Environment Variables

Copy the example file and configure:

```bash
cp .env.example .env
```

Edit `.env`:

```bash
# Required: JWT signing secret (generate with: openssl rand -base64 32)
JWT_SECRET=your-generated-secret-here

# Optional: Seed initial admin and user accounts
SEED_ADMIN_PASSWORD=admin123
SEED_USER_PASSWORD=user123
```

> The `.env` file is automatically picked up by Spring Boot. Alternatively, you can export variables directly:
> ```bash
> export JWT_SECRET=$(openssl rand -base64 32)
> export SEED_ADMIN_PASSWORD=admin123
> export SEED_USER_PASSWORD=user123
> ```

---

### 3. Backend

```bash
cd backend

# Build and run
mvn spring-boot:run
```

The server starts on `http://localhost:8080`.

On first startup with `SEED_ADMIN_PASSWORD` and `SEED_USER_PASSWORD` set, the app automatically seeds:
- 4 product categories
- 6 sample products
- 1 admin account
- 1 regular user account

**Run tests:**
```bash
cd backend
mvn test
```

---

### 4. Frontend

```bash
cd frontend
npm install
npm run dev
```

The app is available at `http://localhost:5173`.

---

## Default Seed Credentials

| Role  | Email                 | Password  |
|-------|----------------------|-----------|
| Admin | admin@specsheet.com  | admin123  |
| User  | user@specsheet.com   | user123   |

> Change these via the `.env` file before first run, or update passwords in the admin panel.

---

## API Endpoints

### Public & User Endpoints
| Method | Path                | Auth | Description                 |
|--------|---------------------|------|-----------------------------|
| POST   | /api/auth/register  | -    | Register new user           |
| POST   | /api/auth/login     | -    | Login, receive JWT          |
| GET    | /api/categories     | -    | List categories             |
| GET    | /api/products       | -    | List products (filterable)  |
| GET    | /api/products/{id}  | -    | Get product detail          |
| POST   | /api/orders         | USER | Place order                 |
| GET    | /api/orders/my      | USER | My orders                   |

### Admin Endpoints (RBAC protected)
| Method | Path                                    | Description              |
|--------|-----------------------------------------|--------------------------|
| POST   | /api/admin/categories                   | Create category          |
| PUT    | /api/admin/categories/{id}              | Update category          |
| DELETE | /api/admin/categories/{id}              | Delete category          |
| POST   | /api/admin/products                     | Create product           |
| PUT    | /api/admin/products/{id}                | Update product           |
| DELETE | /api/admin/products/{id}                | Delete product           |
| PATCH  | /api/admin/products/{id}/stock          | Update stock             |
| POST   | /api/admin/products/{id}/datasheet      | Upload/Fetch datasheet   |
| GET    | /api/admin/orders                       | All orders               |
| PATCH  | /api/admin/orders/{id}/status           | Update order status      |

---

## Project Structure

```
specsheet-central/
├── backend/
│   └── src/main/java/com/specsheetcentral/
│       ├── config/          # Security, DataSeeder
│       ├── controller/      # REST & Admin controllers
│       ├── dto/             # Request/response DTOs
│       ├── model/           # JPA entities
│       ├── repository/      # Spring Data repos
│       ├── security/        # JWT auth filters
│       └── service/         # Business logic & File storage
├── frontend/
│   └── src/
│       ├── components/      # UI components & Modals
│       ├── lib/             # API client, types, utils
│       └── routes/          # Page components
└── README.md
```

---

## Common Issues

**Port 8080 already in use:**
```bash
# Kill process on port 8080, or change server.port in application.properties
```

**Database connection failed:**
- Ensure PostgreSQL is running: `sudo systemctl status postgresql`
- Verify database `specsheet_central` exists
- Check credentials in `backend/src/main/resources/application.properties`

**JWT errors:**
- Make sure `JWT_SECRET` is set in `.env` or exported as an environment variable

**Frontend can't reach backend:**
- Ensure the backend is running on port 8080
- Check for CORS issues if running on different hosts

---

## Testing

```bash
cd backend
mvn test
```

The backend includes **72 automated tests** covering repositories, services, and controller integration points.
