# Parse the environment var browsers. If it is not there just test chrome
browsers = ENV['BROWSERS'] || 'chrome,firefox'
BROWSERS = browsers.split(',').map {|s| s.to_sym}

Capybara.register_driver :chrome do |app|
  Capybara::Selenium::Driver.new(app, :browser => :chrome)
end

Capybara.register_driver :firefox do |app|
  Capybara::Selenium::Driver.new(app, :browser => :firefox)
end

Capybara.register_driver :ie8_server do |app|
  Capybara::Selenium::Driver.new(app, :browser => :remote,
                                      :url => 'http://192.171.153.182:4444/wd/hub',
                                      :desired_capabilities => :internet_explorer)
end