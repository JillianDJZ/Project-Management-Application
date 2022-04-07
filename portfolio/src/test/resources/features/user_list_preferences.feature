Feature: User sorting preferences for the user list

  Scenario Outline: Users sorting preference is saved
    Given I am logged in as <User>
    When I sort the list by <Category>
    Then My sorting preference is <Preference>
    Examples:
      | User | Category | OtherUser | Preference |
    | "steve" | "Name ASC" | "steve" | "Name ASC" |

    Scenario Outline:
      Given I am logged in as <User>
      When I sort the list by <Category>
      And Someone else sorts their list by <OtherCategory>
      Then My sorting preference is <Preference>
      Examples:
        | User | Category | OtherCategory | Preference |
      | "steve" | "Name ASC" | "Name DESC" | "Name ASC"