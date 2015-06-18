# Define a ruby module which allows us to repeat capybara examples in 
# multiple browsers. An example usage of this is:
#
#   describe "my responsive site" do
#     sized [:md,:lg] do
#       it "works in large mode" do
#         ... some capybara test
#       end
#     end
#   end
#
module ScreenSize
  def sized(sizes, &block)
    fixed       = MOBILE_BROWSERS.select { |key| sizes.include? key }
    resolutions = SCREENS.select         { |key| sizes.include? key }

    fixed.values.flatten.each do |browser|
      describe "using #{browser}", :type => :feature, :driver => browser do
        class_eval &block
      end
    end

    DESKTOP_BROWSERS.each do |browser|
      resolutions.each do |size, res|
        describe "using #{size} #{browser}", :type => :feature, :driver => browser do
          class_eval { 
            before(:each) do
              page.driver.browser.manage.window.resize_to(res[:width], res[:height]) 
            end
          }
          class_eval &block
        end
      end
    end

  end

  RSpec.configure do |config|
    config.extend ScreenSize
  end
end
