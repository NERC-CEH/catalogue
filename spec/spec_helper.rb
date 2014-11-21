require 'rubygems'
require 'yaml'
require 'rdf/rdfa'
require 'rspec/retry'
require 'capybara/rspec'
require 'selenium/webdriver'
require 'socket'
require 'browsers_helper'
require 'screen_size'
require 'rest_client'
require 'nokogiri'

# Get the hostname of the box which this server is testing from
hostname = Socket.gethostbyname(Socket.gethostname).first

APP_HOST = "https://#{hostname}:8080"

Capybara.app_host = APP_HOST
Capybara.run_server = false
Capybara.default_selector = :css

$site = RestClient::Resource.new(APP_HOST, :verify_ssl => OpenSSL::SSL::VERIFY_NONE)

METADATA_IDS = YAML::load_file('documents.yaml')

RSpec.configure do |config|
  config.default_retry_count = 10
  config.default_sleep_interval = 5
end
