package com.dvdrental.db.steps;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import com.dvdrental.db.dao.RevenueDao;
import com.dvdrental.db.hooks.DbHooks;
import static org.junit.Assert.*;
import java.sql.SQLException;
import java.util.*;

public class RevenueSteps {
    private RevenueDao revenueDao = new RevenueDao();
    private List<Map<String, Object>> revenueData;

    @When("user fetches revenue by category")
    public void user_fetches_revenue() throws SQLException {
        revenueDao.connect();
        revenueData = revenueDao.getRevenueByCategory();
        revenueDao.disconnect();
        StringBuilder output = new StringBuilder("[RESULT] Revenue by category: " + revenueData.size() + " categories\n");
        for (int i = 0; i < Math.min(3, revenueData.size()); i++) {
            Map<String, Object> row = revenueData.get(i);
            output.append("  [").append(i+1).append("] ").append(row.get("category")).append(" = $").append(row.get("total_revenue")).append("\n");
        }
        if (revenueData.size() > 3) output.append("  ... and ").append(revenueData.size() - 3).append(" more");
        DbHooks.getScenario().attach(output.toString(), "text/plain", "revenue_by_category");
    }

    @Then("revenue data should have {int} categories")
    public void revenue_data_should_have_categories(int expectedCount) {
        assertEquals("Expected " + expectedCount + " categories, got " + revenueData.size(),
                     expectedCount, revenueData.size());
    }

    @Then("revenue should be ordered by total_revenue descending")
    public void revenue_should_be_ordered_descending() {
        for (int i = 1; i < revenueData.size(); i++) {
            java.math.BigDecimal prev = (java.math.BigDecimal) revenueData.get(i - 1).get("total_revenue");
            java.math.BigDecimal curr = (java.math.BigDecimal) revenueData.get(i).get("total_revenue");
            assertTrue("Revenue not in descending order",
                       prev.compareTo(curr) >= 0);
        }
    }

    @Then("each revenue record should have category and total_revenue")
    public void each_revenue_record_should_have_fields() {
        assertFalse("Revenue data is empty", revenueData.isEmpty());
        for (Map<String, Object> row : revenueData) {
            assertNotNull("category should not be null", row.get("category"));
            assertNotNull("total_revenue should not be null", row.get("total_revenue"));
        }
    }
}
