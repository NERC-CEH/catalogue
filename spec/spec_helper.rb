require 'rubygems'
require 'capybara/rspec'
require 'selenium/webdriver'
require 'headless'
require 'socket'
require 'browsers_helper'
require 'rest_helper'

if ENV['HEADLESS'] == 'true'
  headless = Headless.new
  headless.start
  at_exit do
    headless.destroy
  end
end

# Get the hostname of the box which this server is testing from
hostname = Socket.gethostbyname(Socket.gethostname).first

Capybara.app_host = "https://#{hostname}:8080"
Capybara.run_server = false
Capybara.default_selector = :css