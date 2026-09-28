# PostgreSQL Database Automation

**database-automation-java-jdbc-postgresql — Quick Reference**

---

## Overview

| Metric | Value |
|--------|-------|
| **Test Scenarios** | 10 |
| **Feature Files** | 4 |
| **DAO Classes** | 5 |
| **Database Tables** | 15+ |

## Tech Stack

- **Java 11** | **JDBC** | **PostgreSQL 18** | **Cucumber 7.34.9** | **JUnit 4.13.2** | **Maven 3.9.9**

## Architecture

**Feature Files (Gherkin) → Step Definitions → DAO Services → JDBC Connections → PostgreSQL DB**

## Project Structure

```
src/test/java/com/dvdrental/db/
├── config/
│   └── DbConfig.java
├── dao/
│   ├── CustomerDao.java
│   ├── RentalDao.java
│   └── RevenueDao.java
├── steps/
│   ├── CrudSteps.java
│   ├── RentalHistorySteps.java
│   ├── RevenueSteps.java
│   └── ConstraintSteps.java
├── hooks/
│   └── DbHooks.java
└── runners/
    └── DvdRentalRunner.java

src/test/resources/
├── db.properties
├── cucumber.properties
└── features/
    ├── crud_operations.feature
    ├── rental_history.feature
    ├── revenue_by_category.feature
    └── data_integrity.feature
```

### Package Responsibilities

- **config/** — Database connection configuration (loads db.properties)
- **dao/** — Data Access Objects: CustomerDao, RentalDao, RevenueDao encapsulate SQL queries
- **steps/** — Cucumber step definitions: CrudSteps, RentalHistorySteps, RevenueSteps, ConstraintSteps
- **hooks/** — Test lifecycle: @Before (DB connect), @After (cleanup), Scenario logging
- **runners/** — Cucumber test runner with JSON + pretty output plugins
- **features/** — Gherkin feature files (BDD scenarios)

## Test Scenarios (10 Total)

| Feature | Scenarios | Type | Coverage |
|---------|-----------|------|----------|
| `crud_operations` | 3 (Outline) | Functional | INSERT address/customer, SELECT email, UPDATE email, DELETE customer with cleanup |
| `rental_history` | 3 | Functional | 5-table JOIN (customer→rental→inventory→film→category), ASC/DESC sort validation |
| `revenue_by_category` | 1 | Functional | GROUP BY category, SUM(amount), ORDER BY revenue DESC, count validation |
| `data_integrity` | 3 | Negative | FK constraint (invalid customer_id), unique constraint (duplicate email), NOT NULL (missing address_id) |

## Database Design

### DVD Rental Schema (15+ Tables)

PostgreSQL sample database from [postgresqltutorial.com](https://www.postgresqltutorial.com/)

- `customer` — customer_id (PK), first_name, last_name, email, address_id (FK)
- `address` — address_id (PK), address, district, city_id (FK), postal_code, phone
- `rental` — rental_id (PK), rental_date, customer_id (FK), inventory_id (FK)
- `inventory` — inventory_id (PK), film_id (FK), store_id (FK)
- `film` — film_id (PK), title, release_year, rating
- `film_category` — film_id (FK), category_id (FK)
- `category` — category_id (PK), name
- `payment` — payment_id (PK), customer_id (FK), rental_id (FK), amount, payment_date

## Key Technical Patterns

### JDBC Connection Management

```java
Class.forName(DbConfig.getDriver());
Connection conn = DriverManager.getConnection(
  DbConfig.getUrl(), 
  DbConfig.getUser(), 
  DbConfig.getPassword()
);
try (PreparedStatement stmt = conn.prepareStatement(sql)) {
  stmt.setInt(1, customerId);
  ResultSet rs = stmt.executeQuery();
}
```

### Multi-table JOIN Query

```sql
SELECT c.first_name || ' ' || c.last_name AS full_name,
       r.rental_date, f.title, ct.name AS category
FROM rental r
JOIN customer c USING (customer_id)
JOIN inventory i USING (inventory_id)
JOIN film f USING (film_id)
JOIN film_category fc USING (film_id)
JOIN category ct USING (category_id)
WHERE c.customer_id = ?
ORDER BY r.rental_date ASC
```

### Aggregate with GROUP BY

```sql
SELECT ct.name AS category,
       SUM(p.amount) AS total_revenue
FROM payment p
JOIN rental r ON p.rental_id = r.rental_id
JOIN inventory i ON r.inventory_id = i.inventory_id
JOIN film f ON i.film_id = f.film_id
JOIN film_category fc ON f.film_id = fc.film_id
JOIN category ct ON fc.category_id = ct.category_id
GROUP BY ct.name
ORDER BY total_revenue DESC
```

### Scenario Outline with Examples

```gherkin
Scenario Outline: Insert customer
  When user inserts customer <id> "<first>" "<last>"
  Then customer <id> email should be "<email>"

Examples:
  | id  | first | last  | email         |
  | 600 | Angel | Broom | angel@abc.com |
  | 601 | Fairy | Smith | fairy@abc.com |
```

### Constraint Violation Testing

```java
@When("user tries to insert rental with invalid customer ID")
public void invalid_rental() {
  try {
    String sql = "INSERT INTO rental (inventory_id, customer_id, rental_date) 
                   VALUES (1, 9999, NOW())";
    stmt.executeUpdate(sql);
  } catch (SQLException e) {
    lastException = e;
  }
}

@Then("foreign key constraint should be violated")
public void fk_violated() {
  assertTrue(
    lastException.getMessage().contains("foreign key") 
    || lastException.getMessage().contains("violates")
  );
}
```

### Response Logging to Cucumber Report

```java
@Then("customer {int} email should be {string}")
public void customer_email(int id, String expected) {
  String actual = dao.getCustomerEmail(id);
  String message = "[RESULT] Customer " + id + " email: " + actual;
  
  // Attach to Cucumber HTML report
  DbHooks.getScenario().attach(message, "text/plain", "email_" + id);
  
  assertEquals(expected, actual);
}
```

## Running Tests

### Maven Commands

```bash
# Run all tests (default db-all profile)
mvn clean verify

# Run with specific profile
mvn clean verify -P db-all
mvn clean verify -P db-crud

# Skip tests
mvn clean install -DskipTests
```

## Reports & CI/CD

### Cucumber HTML Reports

- **Local:** `target/cucumber-html-reports/overview-features.html`
- **Jenkins:** Published via "Publish HTML reports" post-build action
- **Live URL:** https://jgupta-git.github.io/database-automation-java-jdbc-postgresql/

### Jenkins Integration

- **Job Name:** psql_run
- **Build Command:** `mvn clean verify -P db-all`
- **Report Archive:** `target/cucumber-html-reports`
- **Report URL Pattern:** `http://localhost:8080/job/psql_run/lastSuccessfulBuild/Cucumber_20Report/`

### GitHub Pages Deployment

Reports auto-publish to `gh-pages` branch after successful Jenkins run. Accessible at:

```
https://jgupta-git.github.io/database-automation-java-jdbc-postgresql/
```

## Performance Notes

- All 10 tests execute in ~1.5-2 seconds locally
- Jenkins runs complete in ~8-9 seconds including Maven setup
- Cucumber reporting plugin adds ~2 seconds for HTML generation
- @Before/@After hooks handle connection pooling via static DAO initialization
- PreparedStatements prevent SQL injection and cache execution plans

## References

- **PostgreSQL:** [Official Documentation](https://www.postgresql.org/docs/)
- **JDBC:** [Oracle JDBC Tutorial](https://docs.oracle.com/javase/tutorial/jdbc/)
- **Cucumber:** [Behavior Driven Development](https://cucumber.io/docs/)
- **DVD Rental Database:** [Download & Setup](https://www.postgresqltutorial.com/postgresql-getting-started/postgresql-sample-database/)
- **GitHub Repository:** [jgupta-git/database-automation-java-jdbc-postgresql](https://github.com/jgupta-git/database-automation-java-jdbc-postgresql)

---

**Last updated: 2026-09-28** | Built with Java 11 • JDBC • PostgreSQL • Cucumber • Maven
