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
  :xs  => [],
  :sm  => [ :nexus5, :ipad2 ],
  :md  => [ :Nexus7 ],
  :lg  => []
}

DESKTOP_BROWSERS = [ :chrome, :firefox, :ie ]

SELENIUM_DRIVERS = {
  htcdesire: {
    :platformName => 'Android',
    :deviceName   => 'HTC Desire',
    :app          => 'Chrome',
    :udid         => 'SH2CZLY05396'},
  nexus5: {
    :platformName => 'Android',
    :deviceName   => 'Nexus 5',
    :app          => 'Chrome',
    :udid         => '02fbd6f32108304f'},
  nexus7: {
    :platformName => 'Android',
    :deviceName   => 'Nexus 7',
    :app          => 'Chrome',
    :udid         => '073266a1'},
  chrome: {
    :browserName  => 'chrome'},
  firefox: {
    :browserName  => 'firefox'},
  ie: {
    :platform     => 'WINDOWS',
    :browserName  => 'internet explorer'}
}

SELENIUM_DRIVERS.each do | name, capabilities |
  Capybara.register_driver name do |app|
    Capybara::Selenium::Driver.new(app, :browser => :remote,
                                        :url => 'http://bamboo.ceh.ac.uk:4444/wd/hub',
                                        :desired_capabilities => capabilities)
  end
end
