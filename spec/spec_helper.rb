require 'rubygems'
require 'yaml'
require 'rspec/retry'
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

METADATA_IDS = YAML::load_file('documents.yaml')

RSpec.configure do |config|
  config.default_retry_count = 10
  config.default_sleep_interval = 5
end
