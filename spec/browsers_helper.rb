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
  :xs  => [ :htcdesire ],
  :sm  => [ :nexus5, :ipad2 ],
  :md  => [ :nexus7 ],
  :lg  => []
}

DESKTOP_BROWSERS = [ :chrome, :firefox, :ie, :safari ]

SELENIUM_DRIVERS = {
  htcdesire: {
    :platform     => 'ANDROID',
    :platformName => 'ANDROID',
    :deviceName   => 'HTC Desire',
    :app          => 'Chrome',
    :udid         => 'SH2CZLY05396'},
  nexus5: {
    :platform     => 'ANDROID',
    :platformName => 'ANDROID',
    :deviceName   => 'Nexus 5',
    :app          => 'Chrome',
    :udid         => '02fbd6f32108304f'},
  nexus7: {
    :platform     => 'ANDROID',
    :platformName => 'ANDROID',
    :deviceName   => 'Nexus 7',
    :app          => 'Chrome',
    :udid         => '073266a1'},
  ipad2: {
    :platform          => 'MAC',
    :platformName      => 'iOS',
    :platformVersion   => '8.3',
    :browserName       => 'Appium iOS',
    :autoAcceptAlerts  => true,
    :newCommandTimeout => 6000,
    :orientation       => 'landscape',
    :deviceName        => 'iPad 2'},
  chrome: {
    :platform     => 'LINUX',
    :browserName  => 'chrome'},
  firefox: {
    :platform     => 'LINUX',
    :browserName  => 'firefox'},
  safari: {
    :platform     => 'MAC',
    :browserName  => 'safari'},
  ie: {
    :platform     => 'WINDOWS',
    :browserName  => 'internet explorer'}
}

# SELENIUM_DRIVERS.each do | name, capabilities |
#   Capybara.register_driver name do |app|
#     Capybara::Selenium::Driver.new(app, :browser => :remote,
#                                         :url => 'http://bamboo.ceh.ac.uk:4444/wd/hub',
#                                         :desired_capabilities => capabilities)
#   end
# end

Capybara.register_driver :chrome do |app|
  Capybara::Selenium::Driver.new(app, :browser => :remote,
                                      :url => 'http://chrome:4444/wd/hub',
                                      :desired_capabilities => {:browserName  => 'chrome'})
end
Capybara.register_driver :firefox do |app|
  Capybara::Selenium::Driver.new(app, :browser => :remote,
                                      :url => 'http://firefox:4444/wd/hub',
                                      :desired_capabilities => {:browserName  => 'firefox'})
end
