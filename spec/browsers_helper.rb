require 'open-uri'

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
SCREENS = {
  xs: { width: 480 , height: 768 },
  sm: { width: 767,  height: 768 },
  md: { width: 979,  height: 768 },
  lg: { width: 1200, height: 768 }
}

MOBILE_BROWSERS = {
  :xs  => [:"HTC Desire X"],
  :sm  => [:ipad2],
  :md  => [:"Nexus 5", :"Nexus 7"],
  :lg  => []
}

DESKTOP_BROWSERS = [:chrome, :firefox, :ie_server]

# Read the devices which are currently registered on selendroid and register
# each as a driver in the application
selendroid = JSON.parse(open('http://lapc011.nerc-lancaster.ac.uk:4444/wd/hub/status').read)

selendroid['value']['supportedDevices'].each {|device|
  Capybara.register_driver device['model'].to_sym do |app|
    Capybara::Selenium::Driver.new(app, :browser => :remote,
                                        :url => 'http://lapc011.nerc-lancaster.ac.uk:4444/wd/hub',
                                        :desired_capabilities => {
                                          :browserName       => 'android',
                                          :javascriptEnabled => true,
                                          :serial            => device['serial']})
  end
}

Capybara.register_driver :ipad2 do |app|
  Capybara::Selenium::Driver.new(app, :browser => :remote,
                                      :url => 'http://212.219.37.177:4723/wd/hub',
                                      :desired_capabilities => {
                                        :platformName       => 'iOS',
                                        :platformVersion    => '8.1',
                                        :browserName        => 'safari',
                                        :autoAcceptAlerts   => true,
                                        :newCommandTimeout  => 6000,
                                        :deviceName         => 'iPad 2'})
end

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
