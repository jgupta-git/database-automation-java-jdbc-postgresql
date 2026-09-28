package com.dvdrental.db.dao;

import com.dvdrental.db.config.DbConfig;
import java.sql.*;
import java.util.*;

public class RentalDao {
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

    public List<Map<String, Object>> getRentalHistory(int customerId) throws SQLException {
        String sql = "SELECT c.first_name || ' ' || c.last_name AS full_name, " +
                     "r.rental_date, f.title, ct.name AS category " +
                     "FROM rental r " +
                     "JOIN customer c USING (customer_id) " +
                     "JOIN inventory i USING (inventory_id) " +
                     "JOIN film f USING (film_id) " +
                     "JOIN film_category fc USING (film_id) " +
                     "JOIN category ct USING (category_id) " +
                     "WHERE c.customer_id = ? " +
                     "ORDER BY full_name, r.rental_date";

        List<Map<String, Object>> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("full_name", rs.getString("full_name"));
                row.put("rental_date", rs.getTimestamp("rental_date"));
                row.put("title", rs.getString("title"));
                row.put("category", rs.getString("category"));
                results.add(row);
            }
        }
        return results;
    }

    public int getRentalHistoryRowCount(int customerId) throws SQLException {
        return getRentalHistory(customerId).size();
    }

    public List<Map<String, Object>> getRentalHistorySorted(int customerId, String order) throws SQLException {
        String sql = "SELECT c.first_name || ' ' || c.last_name AS full_name, " +
                     "r.rental_date, f.title, ct.name AS category " +
                     "FROM rental r " +
                     "JOIN customer c USING (customer_id) " +
                     "JOIN inventory i USING (inventory_id) " +
                     "JOIN film f USING (film_id) " +
                     "JOIN film_category fc USING (film_id) " +
                     "JOIN category ct USING (category_id) " +
                     "WHERE c.customer_id = ? " +
                     "ORDER BY r.rental_date " + order;

        List<Map<String, Object>> results = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("full_name", rs.getString("full_name"));
                row.put("rental_date", rs.getTimestamp("rental_date"));
                row.put("title", rs.getString("title"));
                row.put("category", rs.getString("category"));
                results.add(row);
            }
        }
        return results;
    }

    public Timestamp getFirstRentalDate(int customerId, String order) throws SQLException {
        List<Map<String, Object>> results = getRentalHistorySorted(customerId, order);
        if (!results.isEmpty()) {
            return (Timestamp) results.get(0).get("rental_date");
        }
        return null;
    }
}
