require 'rubygems'
require 'yaml'
require 'rdf/rdfxml'
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

METADATA_IDS = [
  '1d859249-e6af-48f4-9fd6-5f8401bc1e4e',
  '1e7d5e08-9e24-471b-ae37-49b477f695e3',
  '2d023ce9-6dbe-4b4f-a0cd-34768e1455ae',
  '571a676f-6c32-489b-b7ec-18dcc617a9f1',
  '6795cf1b-9204-451e-99f1-f9b90658537f',
  'bb2d7874-7bf4-44de-aa43-348bd684a2fe',
  'fb495d1b-80a3-416b-8dc7-c85ed22ed1e3',
  'ff55462e-38a4-4f30-b562-f82ff263d9c3'
]

RSpec.configure do |config|
  config.default_retry_count = 10
end
