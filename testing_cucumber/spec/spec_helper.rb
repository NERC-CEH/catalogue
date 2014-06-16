require 'rubygems'
require 'capybara/rspec'

Capybara.run_server = false
Capybara.default_driver = :selenium
Capybara.default_selector = :css