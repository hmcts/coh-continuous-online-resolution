Feature: Online hearing

  Background:
    Given a standard online hearing is created
    Then the response code is 201

  Scenario: Create online hearing
    And the response contains the following text '"online_hearing_id" '
    And the response contains the online hearing UUID

  Scenario: Create duplicate online hearing
    And a standard online hearing is created
    Then the response code is 409
    Then the response contains the following text '"Duplicate case found" '

  Scenario: Retrieve online hearing
    And the response contains the following text '"online_hearing_id" '
    And the response contains the online hearing UUID
    When a get request is sent to ' "/continuous-online-hearings"' for the saved online hearing
    Then the response code is 200
    And the response contains the following text '"case_123" '
    And the response contains 1 panel member

