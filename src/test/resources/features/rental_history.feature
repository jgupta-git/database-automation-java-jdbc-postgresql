@dvdrental @functional @rental-history
Feature: Rental History Multi-Table Join Query
  As a QA engineer
  I want to verify rental history joins across multiple tables
  So that complex relational queries work correctly

  @positive @smoke
  Scenario: Get rental history for existing customer
    When user fetches rental history for customer 1
    Then rental history should have at least 25 records
    And each rental record should have full_name, rental_date, title, and category

  @positive
  Scenario Outline: Get rental history sorted by rental date
    When user fetches rental history for customer <customer_id> sorted "<order>"
    Then rental history should have at least <min_records> records
    And first rental date should not be null
    And rental dates should be ordered "<order>"

    Examples:
      | customer_id | order      | min_records |
      | 1           | ascending  | 25          |
      | 1           | descending | 25          |
