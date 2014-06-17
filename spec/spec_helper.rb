require 'rubygems'
require 'capybara/rspec'
require 'selenium/webdriver'

Capybara.register_driver :chrome do |app|
  Capybara::Selenium::Driver.new(app, :browser => :chrome)
end

Capybara.app_host = 'https://localhost:8080'
Capybara.run_server = false
Capybara.default_driver = :chrome
Capybara.default_selector = :css