Feature: metadata landing page
  Make sure relevant metadata content is visible
  
  Scenario: A user looks for the title of a metadata page
    When I visit metadata ff55462e-38a4-4f30-b562-f82ff263d9c3
    Then the title should contain "United Kingdom Butterfly Monitoring Scheme: collated indices 2011"
