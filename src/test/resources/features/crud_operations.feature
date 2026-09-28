@dvdrental @functional @crud
Feature: CRUD Operations on Customer
  As a QA engineer
  I want to verify CRUD operations on customer and address tables
  So that data integrity is maintained

  @positive @smoke
  Scenario Outline: Insert, read, update, and delete customer
    When user inserts address <address_id> with "<address>" "<district>" <city_id> "<postal_code>" "<phone>"
    And user inserts customer <customer_id> "<first_name>" "<last_name>" "<email>" <store_id> <address_id>
    Then customer <customer_id> email should be "<email>"
    When user updates customer <customer_id> email to "<new_email>"
    Then customer <customer_id> email should be "<new_email>"
    When user deletes customer <customer_id>
    Then customer <customer_id> should not exist

    Examples:
      | address_id | address                  | district       | city_id | postal_code | phone        | customer_id | first_name | last_name | email              | store_id | new_email           |
      | 606        | 1326 Fukuyama Street     | Heilongjiang   | 537     | 27107       | 5666667788   | 600         | Angel      | Broom     | abroom@abc.com     | 1        | abroom@new.com      |
      | 607        | 1327 Fukuyama Street     | Heilongjiang   | 537     | 27107       | 5666667788   | 601         | Fairy      | Smith     | jane.smith@abc.com | 1        | fairy.smith@new.com |
      | 608        | 1328 Fukuyama Street     | Heilongjiang   | 537     | 27107       | 5666667788   | 602         | Bob        | John      | bob.john@abc.com   | 1        | bob.john@new.com    |
