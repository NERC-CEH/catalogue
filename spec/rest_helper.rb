require 'rubygems'
require 'rest_client'
require 'nokogiri'

$site = RestClient::Resource.new(
  'https://localhost:8080',
  :verify_ssl => OpenSSL::SSL::VERIFY_NONE)