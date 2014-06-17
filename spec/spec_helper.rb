require 'rubygems'
require 'capybara/rspec'
require 'capybara/poltergeist'

Capybara.app_host = 'https://localhost:8080'
Capybara.run_server = false
Capybara.default_driver = :poltergeist
Capybara.default_selector = :css