# Parse the environment var browsers. If it is not there just test chrome
browsers = ENV['BROWSERS'] || 'chrome,firefox'
BROWSERS = browsers.split(',').map {|s| s.to_sym}
# These are some common screen widths according to an answer on [stackoverflow]
#  (http://ux.stackexchange.com/questions/28124/what-is-the-current-pixel-width-standard-for-a-websites-content-area) 
#
# Phone - 480px
# Tablet - 767px
# Desktop - 979px
# Large Display - 1200px
#
# Whether or not you believe that these are common resolutions, I think it is a good
# starting place.
#
SUPPORTED_WIDTHS = [480, 767, 979, 1200]

Capybara.register_driver :chrome do |app|
  Capybara::Selenium::Driver.new(app, :browser => :chrome)
end

Capybara.register_driver :firefox do |app|
  Capybara::Selenium::Driver.new(app, :browser => :firefox)
end

Capybara.register_driver :ie_server do |app|
  Capybara::Selenium::Driver.new(app, :browser => :remote,
                                      :url => 'http://192.171.153.182:4444/wd/hub',
                                      :desired_capabilities => :internet_explorer)
end
