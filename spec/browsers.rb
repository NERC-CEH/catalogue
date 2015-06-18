module ScreenSize
  SCREENS = {
    xs: { width: 590 , height: 768 },
    sm: { width: 1025, height: 768 },
    md: { width: 1280, height: 768 },
    lg: { width: 1280, height: 768 }
  }
  def sized(sizes, &block)
    fixed       = DISPLAYS.select { |key| sizes.include? key }
    resolutions = SCREENS.select  { |key| sizes.include? key }

    fixed.values.flatten.each do |browser|
      describe "using #{browser}", :type => :feature, :driver => browser do
        class_eval &block
      end
    end

    ADJUSTABLE.each do |browser|
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
end