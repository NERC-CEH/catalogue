require 'rubygems'
require 'capybara/rspec'

Capybara.app_host = 'https://localhost:8080'
Capybara.run_server = false
Capybara.default_driver = :selenium
Capybara.default_selector = :css