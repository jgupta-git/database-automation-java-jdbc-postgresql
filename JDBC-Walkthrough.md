# JDBC Cheat Sheet

A quick reference for connecting to PostgreSQL with JDBC, running SQL, reading results and handling errors. Each section shows the code and then explains what it does.

---

## 0. Imports

```java
import java.sql.*;
```

**What this does:** imports the JDBC types used below: `Connection`, `DriverManager`, `Statement`, `PreparedStatement`, `ResultSet`, `DatabaseMetaData`, `SQLException`, `Types` and `Timestamp`. The wildcard is what makes `Connection` available without its own import line. Examples that use `Properties` or `BigDecimal` need `import java.util.Properties;` and `import java.math.BigDecimal;`.

**Driver:** the PostgreSQL driver must be on the classpath. In the database repo it is declared in `pom.xml`.

---

## 1. Connect

```java
String url  = "jdbc:postgresql://localhost:5432/dvdrental";
String user = "postgres";
String pass = "your_password";

Connection conn = DriverManager.getConnection(url, user, pass);
```

**What this does:**
- `url` names the driver (`jdbc:postgresql:`), the host and port (`localhost:5432`) and the database (`dvdrental`).
- `DriverManager.getConnection` finds the driver, opens a TCP connection to PostgreSQL, logs in, and returns a `Connection` object. All later database calls go through this object.

The same connection with a `Properties` object, which is useful when extra driver options are needed:

```java
Properties p = new Properties();
p.setProperty("user", user);
p.setProperty("password", pass);
Connection conn = DriverManager.getConnection(url, p);
```

**What this does:** passes the login details as properties instead of separate arguments. Other driver options are added the same way.

Checking that the connection is alive:

```java
boolean ok     = conn.isValid(5);   // true if the server answers within 5 seconds
boolean closed = conn.isClosed();   // false while the connection is open
```

**What this does:** `isValid(5)` sends a lightweight check to the server and waits up to 5 seconds. `isClosed()` checks only the local object, not the server.

---

## 2. Statement (plain SQL)

`Statement` is used for SQL with no values supplied at runtime.

```java
try (Statement st = conn.createStatement()) {

    ResultSet rs = st.executeQuery("SELECT title FROM film LIMIT 5");
    // SELECT: returns rows

    int rows = st.executeUpdate("DELETE FROM rental WHERE rental_id = 1");
    // INSERT / UPDATE / DELETE / DDL: returns how many rows changed

    boolean hasRows = st.execute("SELECT 1");
    // any SQL: returns true if it produced a ResultSet, false if it produced a count
}
```

**What this does:**
- `createStatement()` creates an object that runs SQL on this connection.
- `executeQuery(sql)` runs a SELECT and returns a `ResultSet` (the rows).
- `executeUpdate(sql)` runs a statement that changes data and returns the number of affected rows. It is used for INSERT, UPDATE, DELETE and CREATE/DROP.
- `execute(sql)` runs any statement and reports which kind of result came back. It is used when the statement type is not known in advance.
- `try (...)` closes the statement automatically at the end of the block.

**Warning:** SQL must never be built by joining user input into the string. `PreparedStatement` (next section) is the safe alternative.

---

## 3. PreparedStatement (SQL with parameters)

```java
String sql = "SELECT * FROM customer WHERE email = ? AND store_id = ?";

try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setString(1, "mary@x.com");
    ps.setInt(2, 1);

    try (ResultSet rs = ps.executeQuery()) {
        // rows are read here (section 4)
    }
}
```

**What this does:**
- `?` marks a placeholder. The database receives the SQL shape first and the values separately afterwards.
- `setString(1, ...)` fills the first `?`. Parameter numbers start at 1, not 0.
- Because the value is sent separately, it cannot change the SQL. This prevents SQL injection.
- `executeQuery()` runs the prepared SELECT. The SQL string is not passed again.

Other setters:

```java
ps.setInt(1, 10);
ps.setLong(2, 10000L);
ps.setDouble(3, 4.99);
ps.setBoolean(4, true);
ps.setDate(5, java.sql.Date.valueOf("2024-01-31"));
ps.setTimestamp(6, Timestamp.valueOf("2024-01-31 10:00:00"));
ps.setNull(7, Types.VARCHAR);   // sends SQL NULL with its type
```

**What this does:** each setter maps a Java type to a SQL type. `setNull` needs the SQL type because the database cannot infer it from a Java `null`.

INSERT, UPDATE or DELETE with parameters:

```java
String sql = "UPDATE customer SET active = ? WHERE customer_id = ?";
try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setBoolean(1, false);
    ps.setInt(2, 42);
    int changed = ps.executeUpdate();   // number of rows updated
}
```

**What this does:** same pattern as a query, except `executeUpdate()` returns the row count instead of a `ResultSet`.

---

## 4. Reading a ResultSet

```java
try (ResultSet rs = st.executeQuery(
        "SELECT customer_id, first_name, create_date FROM customer")) {

    while (rs.next()) {
        int id          = rs.getInt("customer_id");
        String name     = rs.getString(2);          // by column position (1-based)
        Timestamp ts    = rs.getTimestamp("create_date");
        boolean isNull  = rs.wasNull();             // true if the last value read was NULL
    }
}
```

**What this does:**
- A `ResultSet` starts before the first row. `rs.next()` moves to the next row and returns `false` when no rows remain, which is why the loop is `while (rs.next())`.
- `getInt("customer_id")` reads a value by column name. `getString(2)` reads by position. Names are safer if the query changes; positions are shorter.
- `wasNull()` matters for numbers. `getInt` returns `0` for SQL NULL, so `wasNull()` is checked when NULL is possible.

Other getters: `getLong`, `getDouble`, `getBigDecimal`, `getBoolean`, `getDate`, `getTimestamp`, `getObject` (any type).

Column metadata:

```java
ResultSetMetaData md = rs.getMetaData();
int columns  = md.getColumnCount();    // how many columns the query returned
String label = md.getColumnLabel(1);   // name of column 1
```

**What this does:** reports the shape of the result without knowing it in advance.

**Scrolling:** `first()`, `last()` and `previous()` work only when the statement is created with a scrollable type:

```java
Statement st = conn.createStatement(
        ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
```

**What this does:** asks the driver to keep the result so it can move backwards. The default type is forward-only.

---

## 5. Getting the generated ID after an INSERT

```java
String sql = "INSERT INTO address (address, district, city_id, postal_code, phone) "
           + "VALUES (?, ?, ?, ?, ?)";

try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    ps.setString(1, "1 Main St");
    ps.setString(2, "Central");
    ps.setInt(3, 300);
    ps.setString(4, "12345");
    ps.setString(5, "555-0100");
    ps.executeUpdate();

    try (ResultSet keys = ps.getGeneratedKeys()) {
        if (keys.next()) {
            int newId = keys.getInt(1);
        }
    }
}
```

**What this does:**
- `RETURN_GENERATED_KEYS` tells the driver to keep the auto-generated primary key (for example, a serial column) when the insert runs.
- `getGeneratedKeys()` returns those keys in a `ResultSet`, which is read the same way as any query result.

---

## 6. Transactions

```java
conn.setAutoCommit(false);        // start a transaction (the default is true)
try {
    // one or more INSERT / UPDATE / DELETE statements
    conn.commit();                // make all of them permanent
} catch (SQLException e) {
    conn.rollback();              // undo all of them
    throw e;
} finally {
    conn.setAutoCommit(true);     // restore the default
}
```

**What this does:**
- By default, each statement commits on its own. `setAutoCommit(false)` groups statements into one transaction.
- `commit()` saves all of them. If any step fails, `rollback()` undoes all of them, so the database is never left half-updated.
- The `finally` block restores auto-commit, so later code on the same connection behaves normally.

Savepoints (partial rollback):

```java
Savepoint sp = conn.setSavepoint();
// ... some statements ...
conn.rollback(sp);   // undo only the work done after the savepoint
```

**What this does:** rolls back to a marked point and keeps earlier work in the same transaction.

---

## 7. Batch (many statements in one round trip)

```java
String sql = "INSERT INTO language (name) VALUES (?)";
try (PreparedStatement ps = conn.prepareStatement(sql)) {
    for (String n : new String[]{"Klingon", "Elvish"}) {
        ps.setString(1, n);
        ps.addBatch();                  // queue this set of values
    }
    int[] counts = ps.executeBatch();   // send the queue at once
}
```

**What this does:** `addBatch()` stores each set of parameters in memory. `executeBatch()` sends them together, which is much faster than one round trip per row. `counts` holds the result for each queued row.

---

## 8. Database metadata (what tables exist)

```java
DatabaseMetaData md = conn.getMetaData();

String product = md.getDatabaseProductName();     // "PostgreSQL"
String version = md.getDatabaseProductVersion();

ResultSet tables = md.getTables(null, "public", "%", new String[]{"TABLE"});
ResultSet cols   = md.getColumns(null, "public", "customer", "%");
ResultSet pk     = md.getPrimaryKeys(null, "public", "customer");
ResultSet fk     = md.getImportedKeys(null, "public", "rental");
```

**What this does:**
- `getMetaData()` returns information about the database, not its data.
- `getTables` lists the tables in the `public` schema. `%` is a wildcard that matches all names.
- `getColumns` lists the columns of one table.
- `getPrimaryKeys` and `getImportedKeys` show key relationships, including the foreign keys the table uses.

---

## 9. Errors (SQLException)

```java
try {
    // any JDBC call
} catch (SQLException e) {
    String state = e.getSQLState();   // standard 5-character code
    int code     = e.getErrorCode();  // vendor-specific number
    String msg   = e.getMessage();    // human-readable text
}
```

**What this does:** `SQLException` is thrown when the database rejects a call. `getSQLState()` returns the standard code, which is the value to check in tests because message text can change between database versions.

Common PostgreSQL state codes:

| SQLState | Meaning |
|---|---|
| `23505` | unique violation (duplicate value) |
| `23503` | foreign key violation |
| `23502` | not-null violation |
| `42P01` | table does not exist |
| `42703` | column does not exist |
| `08001` | cannot connect |

**What this does:** lists the states seen most often. Constraint tests assert on the code rather than the message.

---

## 10. Closing resources

```java
try (Connection conn = DriverManager.getConnection(url, user, pass);
     Statement st   = conn.createStatement();
     ResultSet rs   = st.executeQuery("SELECT 1")) {
    // rs, st and conn are used here
}   // all three are closed automatically, in reverse order
```

**What this does:** `try-with-resources` closes each object when the block ends, even when an error is thrown. The close order is `ResultSet`, then `Statement`, then `Connection`. Open connections use up the database's connection limit, so every connection is closed, either in this form or in an `@After` hook.

---

## Quick reference

| Task | Method |
|---|---|
| Open a connection | `DriverManager.getConnection(url, user, pass)` |
| Check the connection | `conn.isValid(5)` |
| Run a SELECT | `executeQuery(sql)` → `ResultSet` |
| Run an INSERT/UPDATE/DELETE | `executeUpdate(sql)` → row count |
| Run any SQL | `execute(sql)` → `boolean` |
| Safe parameters | `prepareStatement(sql)` + `setXxx(index, value)` |
| Read rows | `while (rs.next())` + `rs.getXxx(...)` |
| Manual transaction | `setAutoCommit(false)`, `commit()`, `rollback()` |
| Many inserts | `addBatch()` + `executeBatch()` |
| Get a new ID | `RETURN_GENERATED_KEYS` + `getGeneratedKeys()` |
| Table information | `getMetaData()` |
| Error code | `getSQLState()` |
