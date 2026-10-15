@api @helpdesk
Feature: Helpdesk ticket API
  Repository: hovr_helpdesk_repository.dart

  Background:
    Given I am authenticated as a rider

  # BLOCKED: excluded on the project owner's instruction (2026-10-14): Help and support tickets are not tested because creating or replying to tickets makes real tickets in the shared helpdesk.
  @blocked
  Scenario: List open tickets
    When I call "ListOpenTickets"
    Then the response status is "OK"

  # BLOCKED: excluded on the project owner's instruction (2026-10-14): Help and support tickets are not tested because creating or replying to tickets makes real tickets in the shared helpdesk.
  @smoke @blocked
  Scenario: Create a ticket
    When I call "CreateTicket" with:
      | subject     | Driver didn't arrive                  |
      | description | Waited 20 minutes, driver never came. |
    Then the response contains a ticket id
    And ListOpenTickets contains that ticket

  # BLOCKED: excluded on the project owner's instruction (2026-10-14): Help and support tickets are not tested because creating or replying to tickets makes real tickets in the shared helpdesk.
  @blocked
  Scenario Outline: Create ticket validation
    When I call "CreateTicket" with subject "<subject>" and description "<description>"
    Then the response status is "INVALID_ARGUMENT"

    Examples:
      | subject | description          |
      |         | Some detail here ok  |
      | Driver  |                      |

  # BLOCKED: excluded on the project owner's instruction (2026-10-14): Help and support tickets are not tested because creating or replying to tickets makes real tickets in the shared helpdesk.
  @blocked
  Scenario: Reply to a ticket
    Given I have an open ticket
    When I call "TicketArticle" to add a reply "Any update?"
    Then ListTicketArticles for that ticket contains "Any update?"

  # BLOCKED: needs a ticket owned by another rider.
  # BLOCKED: excluded on the project owner's instruction (2026-10-14): Help and support tickets are not tested because creating or replying to tickets makes real tickets in the shared helpdesk.
  @blocked
  Scenario: Read someone else's ticket
    When I call "ListTicketArticles" for a ticket owned by another rider
    Then the response status is "PERMISSION_DENIED" or "NOT_FOUND"
