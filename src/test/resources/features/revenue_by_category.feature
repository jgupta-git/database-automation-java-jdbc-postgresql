@dvdrental @functional @revenue
Feature: Revenue Aggregation by Category
  As a QA engineer
  I want to verify revenue aggregation across categories
  So that aggregate queries and ordering work correctly

  @positive @smoke
  Scenario: Get revenue by film category
    When user fetches revenue by category
    Then revenue data should have 16 categories
    And each revenue record should have category and total_revenue
    And revenue should be ordered by total_revenue descending
