# Parse the environment var browsers. If it is not there just test chrome
browsers = ENV['BROWSERS'] || 'chrome'
BROWSERS = browsers.split(',').map {|s| s.to_sym}

Capybara.register_driver :chrome do |app|
  Capybara::Selenium::Driver.new(app, :browser => :chrome)
end

Capybara.register_driver :firefox do |app|
  Capybara::Selenium::Driver.new(app, :browser => :firefox)
end

Capybara.register_driver :internet_explorer do |app|
  Capybara::Selenium::Driver.new(app, :browser => :internet_explorer)
end