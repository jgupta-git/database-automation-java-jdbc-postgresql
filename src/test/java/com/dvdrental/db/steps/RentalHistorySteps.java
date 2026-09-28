package com.dvdrental.db.steps;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import com.dvdrental.db.dao.RentalDao;
import static org.junit.Assert.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class RentalHistorySteps {
    private RentalDao rentalDao = new RentalDao();
    private List<Map<String, Object>> rentalHistory;

    @When("user fetches rental history for customer {int}")
    public void user_fetches_rental_history(int customerId) throws SQLException {
        rentalDao.connect();
        rentalHistory = rentalDao.getRentalHistory(customerId);
        rentalDao.disconnect();
        System.out.println("[RESULT] Rental history for customer " + customerId + ": " + rentalHistory.size() + " records");
        for (int i = 0; i < Math.min(3, rentalHistory.size()); i++) {
            Map<String, Object> row = rentalHistory.get(i);
            System.out.println("  [" + (i+1) + "] " + row.get("full_name") + " | " + row.get("rental_date") + " | " + row.get("title"));
        }
        if (rentalHistory.size() > 3) System.out.println("  ... and " + (rentalHistory.size() - 3) + " more");
    }

    @Then("rental history should have at least {int} records")
    public void rental_history_should_have_records(int minRecords) {
        assertTrue("Expected at least " + minRecords + " records, got " + rentalHistory.size(),
                   rentalHistory.size() >= minRecords);
    }

    @Then("each rental record should have full_name, rental_date, title, and category")
    public void each_rental_record_should_have_fields() {
        assertFalse("Rental history is empty", rentalHistory.isEmpty());
        for (Map<String, Object> row : rentalHistory) {
            assertNotNull("full_name should not be null", row.get("full_name"));
            assertNotNull("rental_date should not be null", row.get("rental_date"));
            assertNotNull("title should not be null", row.get("title"));
            assertNotNull("category should not be null", row.get("category"));
        }
    }

    @When("user fetches rental history for customer {int} sorted {string}")
    public void user_fetches_rental_history_sorted(int customerId, String order) throws SQLException {
        rentalDao.connect();
        rentalHistory = rentalDao.getRentalHistorySorted(customerId, order);
        rentalDao.disconnect();
        System.out.println("[RESULT] Rental history for customer " + customerId + " sorted " + order + ": " + rentalHistory.size() + " records");
        System.out.println("  [FIRST] " + rentalHistory.get(0).get("rental_date"));
        System.out.println("  [LAST] " + rentalHistory.get(rentalHistory.size()-1).get("rental_date"));
    }

    @Then("first rental date should not be null")
    public void first_rental_date_should_not_be_null() {
        assertFalse("Rental history is empty", rentalHistory.isEmpty());
        assertNotNull("First rental date should not be null", rentalHistory.get(0).get("rental_date"));
    }

    @Then("rental dates should be ordered {string}")
    public void rental_dates_should_be_ordered(String order) {
        assertFalse("Rental history is empty", rentalHistory.isEmpty());
        for (int i = 1; i < rentalHistory.size(); i++) {
            Timestamp prev = (Timestamp) rentalHistory.get(i - 1).get("rental_date");
            Timestamp curr = (Timestamp) rentalHistory.get(i).get("rental_date");
            if ("ascending".equalsIgnoreCase(order)) {
                assertTrue("Dates not in ascending order", prev.compareTo(curr) <= 0);
            } else {
                assertTrue("Dates not in descending order", prev.compareTo(curr) >= 0);
            }
        }
    }
}
