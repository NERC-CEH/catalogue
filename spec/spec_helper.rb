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

# XXX: Removed global screenshot taking, should be more test specific
#
# Taking shapshots of all tests
# Not too sure if this is a good idea yet but got it in to see how it goes
# RSpec.configure do |c|
#   c.after :each do |x|
#     page.save_screenshot("rspec_screenshots/#{x.metadata[:full_description]}.png")
#   end
# end