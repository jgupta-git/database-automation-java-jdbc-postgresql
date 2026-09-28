package com.dvdrental.db.steps;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import com.dvdrental.db.config.DbConfig;
import static org.junit.Assert.*;
import java.sql.*;

public class ConstraintSteps {
    private Connection connection;
    private SQLException lastException;

    @When("user tries to insert rental with invalid customer ID")
    public void user_tries_invalid_customer_rental() {
        try {
            Class.forName(DbConfig.getDriver());
            connection = DriverManager.getConnection(DbConfig.getUrl(), DbConfig.getUser(), DbConfig.getPassword());
            String sql = "INSERT INTO rental (inventory_id, customer_id, rental_date) VALUES (1, 999999, NOW())";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            lastException = e;
        } catch (ClassNotFoundException e) {
            lastException = new SQLException(e);
        }
    }

    @Then("foreign key constraint should be violated")
    public void foreign_key_constraint_violated() {
        assertNotNull("Expected FK violation exception, but no exception was thrown", lastException);
        String msg = lastException.getMessage().toLowerCase();
        assertTrue("Expected FK violation, got: " + msg,
                   msg.contains("foreign key") || msg.contains("violates") || msg.contains("fk_rental"));
        try { if (connection != null) connection.close(); } catch (SQLException e) {}
    }

    @When("user tries to insert customer with duplicate email")
    public void user_tries_duplicate_email() {
        try {
            Class.forName(DbConfig.getDriver());
            connection = DriverManager.getConnection(DbConfig.getUrl(), DbConfig.getUser(), DbConfig.getPassword());
            String sql = "INSERT INTO customer (customer_id, first_name, last_name, email, store_id, address_id) " +
                        "VALUES (9999, 'Test', 'User', (SELECT email FROM customer LIMIT 1), 1, 1)";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            lastException = e;
        } catch (ClassNotFoundException e) {
            lastException = new SQLException(e);
        }
    }

    @Then("unique constraint should be violated")
    public void unique_constraint_violated() {
        assertNotNull("Expected unique constraint violation", lastException);
        assertTrue("Expected unique constraint message",
                   lastException.getMessage().contains("unique") || lastException.getMessage().contains("duplicate"));
        try { if (connection != null) connection.close(); } catch (SQLException e) {}
    }

    @When("user tries to insert customer without required address_id")
    public void user_tries_null_address_id() {
        try {
            Class.forName(DbConfig.getDriver());
            connection = DriverManager.getConnection(DbConfig.getUrl(), DbConfig.getUser(), DbConfig.getPassword());
            String sql = "INSERT INTO customer (customer_id, first_name, last_name, email, store_id) " +
                        "VALUES (9998, 'Test', 'User', 'test@test.com', 1)";
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            lastException = e;
        } catch (ClassNotFoundException e) {
            lastException = new SQLException(e);
        }
    }

    @Then("not null constraint should be violated")
    public void not_null_constraint_violated() {
        assertNotNull("Expected NOT NULL constraint violation", lastException);
        assertTrue("Expected NOT NULL constraint message",
                   lastException.getMessage().contains("not-null") || lastException.getMessage().contains("NOT NULL"));
        try { if (connection != null) connection.close(); } catch (SQLException e) {}
    }
}
