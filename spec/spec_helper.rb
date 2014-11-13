require 'rubygems'
require 'capybara/rspec'
require 'selenium/webdriver'
require 'socket'
require 'browsers_helper'
require 'screen_size'
require 'rest_helper'

# Get the hostname of the box which this server is testing from
hostname = Socket.gethostbyname(Socket.gethostname).first

Capybara.app_host = "https://#{hostname}:8080"
Capybara.run_server = false
Capybara.default_selector = :css
