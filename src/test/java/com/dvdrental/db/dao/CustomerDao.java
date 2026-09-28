package com.dvdrental.db.dao;

import com.dvdrental.db.config.DbConfig;
import java.sql.*;

public class CustomerDao {
    private Connection connection;

    public void connect() throws SQLException {
        try {
            Class.forName(DbConfig.getDriver());
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL driver not found", e);
        }
        connection = DriverManager.getConnection(DbConfig.getUrl(), DbConfig.getUser(), DbConfig.getPassword());
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public void insertAddress(int addressId, String address, String district, int cityId, String postalCode, String phone) throws SQLException {
        String sql = "INSERT INTO address (address_id, address, district, city_id, postal_code, phone) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, addressId);
            stmt.setString(2, address);
            stmt.setString(3, district);
            stmt.setInt(4, cityId);
            stmt.setString(5, postalCode);
            stmt.setString(6, phone);
            stmt.executeUpdate();
        }
    }

    public void insertCustomer(int customerId, String firstName, String lastName, String email, int storeId, int addressId) throws SQLException {
        String sql = "INSERT INTO customer (customer_id, first_name, last_name, email, store_id, address_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, email);
            stmt.setInt(5, storeId);
            stmt.setInt(6, addressId);
            stmt.executeUpdate();
        }
    }

    public String getCustomerEmail(int customerId) throws SQLException {
        String sql = "SELECT email FROM customer WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        }
        return null;
    }

    public void updateCustomerEmail(int customerId, String newEmail) throws SQLException {
        String sql = "UPDATE customer SET email = ? WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newEmail);
            stmt.setInt(2, customerId);
            stmt.executeUpdate();
        }
    }

    public void deleteCustomer(int customerId) throws SQLException {
        String sql = "DELETE FROM customer WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.executeUpdate();
        }
    }

    public void deleteAddress(int addressId) throws SQLException {
        String sql = "DELETE FROM address WHERE address_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, addressId);
            stmt.executeUpdate();
        }
    }

    public int getCustomerCount(int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customer WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
