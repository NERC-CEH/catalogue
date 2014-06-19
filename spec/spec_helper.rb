require 'rubygems'
require 'capybara/rspec'
require 'selenium/webdriver'
require 'headless'
require 'browsers_helper'

if ENV['HEADLESS'] == 'true'
  headless = Headless.new
  headless.start
  at_exit do
    headless.destroy
  end
end

Capybara.app_host = 'https://localhost:8080'
Capybara.run_server = false
Capybara.default_selector = :css

# Taking shapshots of all tests
# Not too sure if this is a good idea yet but got it in to see how it goes
RSpec.configure do |c|
  c.after :each do |x|
    page.save_screenshot("rspec_screenshots/#{x.metadata[:full_description]}.png")
  end
end