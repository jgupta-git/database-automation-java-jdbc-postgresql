package com.dvdrental.db.steps;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.Given;
import com.dvdrental.db.dao.CustomerDao;
import com.dvdrental.db.hooks.DbHooks;
import static org.junit.Assert.*;
import java.sql.SQLException;

public class CrudSteps {
    private CustomerDao customerDao = DbHooks.getCustomerDao();
    private String lastEmail;

    @When("user inserts address {int} with {string} {string} {int} {string} {string}")
    public void user_inserts_address(int addressId, String address, String district, int cityId, String postalCode, String phone) throws SQLException {
        customerDao.insertAddress(addressId, address, district, cityId, postalCode, phone);
    }

    @When("user inserts customer {int} {string} {string} {string} {int} {int}")
    public void user_inserts_customer(int customerId, String firstName, String lastName, String email, int storeId, int addressId) throws SQLException {
        customerDao.insertCustomer(customerId, firstName, lastName, email, storeId, addressId);
    }

    @Then("customer {int} email should be {string}")
    public void customer_email_should_be(int customerId, String expectedEmail) throws SQLException {
        String actual = customerDao.getCustomerEmail(customerId);
        String message = "[RESULT] Customer " + customerId + " email: " + actual;
        DbHooks.getScenario().log(message);
        assertEquals("Email mismatch", expectedEmail, actual);
        lastEmail = actual;
    }

    @When("user updates customer {int} email to {string}")
    public void user_updates_customer_email(int customerId, String newEmail) throws SQLException {
        customerDao.updateCustomerEmail(customerId, newEmail);
    }

    @When("user deletes customer {int}")
    public void user_deletes_customer(int customerId) throws SQLException {
        customerDao.deleteCustomer(customerId);
    }

    @Then("customer {int} should not exist")
    public void customer_should_not_exist(int customerId) throws SQLException {
        int count = customerDao.getCustomerCount(customerId);
        assertEquals("Customer should not exist", 0, count);
    }
}
