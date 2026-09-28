package com.dvdrental.db.hooks;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import com.dvdrental.db.dao.CustomerDao;
import java.sql.*;

public class DbHooks {
    private static CustomerDao customerDao = new CustomerDao();
    private static Scenario currentScenario;

    public static CustomerDao getCustomerDao() {
        return customerDao;
    }

    public static Scenario getScenario() {
        return currentScenario;
    }

    @Before
    public void setup(Scenario scenario) throws SQLException {
        currentScenario = scenario;
        customerDao.connect();
    }

    @After
    public void cleanup() throws SQLException {
        try {
            customerDao.deleteCustomer(600);
            customerDao.deleteCustomer(601);
            customerDao.deleteCustomer(602);
            customerDao.deleteAddress(606);
            customerDao.deleteAddress(607);
            customerDao.deleteAddress(608);
        } catch (Exception e) {
            // Test data may not exist
        }
        customerDao.disconnect();
    }
}
