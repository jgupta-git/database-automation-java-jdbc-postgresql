# Database Automation — PostgreSQL JDBC

BDD database test framework using Java, JDBC, Cucumber, and PostgreSQL.

## Tech Stack

Java 11 | PostgreSQL 18 | JDBC | Cucumber 7.34.9 | JUnit 4.13.2 | Maven

## Database

**DVD Rental** — PostgreSQL sample database with 15+ tables (customer, rental, film, category, payment, inventory, etc.)

Download from: https://www.postgresqltutorial.com/postgresql-getting-started/postgresql-sample-database/

## Test Scenarios (4)

| Scenario | Type | Coverage |
|----------|------|----------|
| `crud_operations` | Functional | INSERT, SELECT, UPDATE, DELETE on customer/address tables |
| `rental_history` | Functional | 5-table JOIN: customer → rental → inventory → film → category |
| `revenue_by_category` | Functional | GROUP BY, SUM, ORDER BY across payment/rental/film/category |
| `data_integrity` | Negative | FK constraint, unique constraint, NOT NULL constraint violations |

## Setup

1. **Install PostgreSQL 18** from https://www.postgresql.org/download/
2. **Restore dvdrental database**:
   ```bash
   createdb -U postgres dvdrental
   pg_restore -U postgres -d dvdrental dvdrental.tar
   ```
3. **Update db.properties** — set your PostgreSQL password:
   ```
   db.password=your_password_here
   ```

## Run Tests

```bash
mvn clean test
```

## Reports

**Live Cucumber HTML Report:** https://jgupta-git.github.io/database-automation-java-jdbc-postgresql/

Local reports generated at `target/cucumber-html-reports/index.html`

## Project Guide

- **[HTML Guide](database-automation-project-guide.html)** — Interactive web-based guide with styling and diagrams
- **[Markdown Guide](database-automation-project-guide.md)** — Portable markdown format (easily convertible to PDF)

## Architecture

- **config/** — DbConfig loads db.properties
- **dao/** — CustomerDao, RentalDao, RevenueDao encapsulate SQL queries
- **steps/** — CrudSteps, RentalHistorySteps, RevenueSteps, ConstraintSteps
- **hooks/** — DbHooks manages @Before/@After setup/cleanup
- **runners/** — DvdRentalRunner executes all features
