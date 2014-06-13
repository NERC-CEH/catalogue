When /^I visit metadata (.*)$/ do |id|
  visit "https://localhost:8080/documents/#{id}"
end

Then /^the title should contain "(.*)"$/ do |expected_result|
  page.title.starts_with? expected_result
end