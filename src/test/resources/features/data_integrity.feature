@dvdrental @functional @constraints
Feature: Data Integrity Constraint Validation
  As a QA engineer
  I want to verify database constraints are enforced
  So that invalid data is rejected

  @negative
  Scenario: Foreign key constraint violation
    When user tries to insert rental with invalid customer ID
    Then foreign key constraint should be violated

  @negative
  Scenario: Unique constraint violation
    When user tries to insert customer with duplicate email
    Then unique constraint should be violated

  @negative
  Scenario: Not null constraint violation
    When user tries to insert customer without required address_id
    Then not null constraint should be violated
