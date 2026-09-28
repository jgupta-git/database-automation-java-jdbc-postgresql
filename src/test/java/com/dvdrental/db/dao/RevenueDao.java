package com.dvdrental.db.dao;

import com.dvdrental.db.config.DbConfig;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class RevenueDao {
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

    public List<Map<String, Object>> getRevenueByCategory() throws SQLException {
        String sql = "SELECT c.name AS category, SUM(p.amount) AS total_revenue " +
                     "FROM payment p " +
                     "JOIN rental r USING (rental_id) " +
                     "JOIN inventory i USING (inventory_id) " +
                     "JOIN film f USING (film_id) " +
                     "JOIN film_category fc USING (film_id) " +
                     "JOIN category c USING (category_id) " +
                     "GROUP BY c.name " +
                     "ORDER BY total_revenue DESC";

        List<Map<String, Object>> results = new ArrayList<>();
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("category", rs.getString("category"));
                row.put("total_revenue", rs.getBigDecimal("total_revenue"));
                results.add(row);
            }
        }
        return results;
    }

    public int getCategoryCount() throws SQLException {
        return getRevenueByCategory().size();
    }

    public BigDecimal getRevenueForCategory(String category) throws SQLException {
        List<Map<String, Object>> results = getRevenueByCategory();
        for (Map<String, Object> row : results) {
            if (category.equals(row.get("category"))) {
                return (BigDecimal) row.get("total_revenue");
            }
        }
        return null;
    }
}
