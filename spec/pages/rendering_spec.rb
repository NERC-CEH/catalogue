BROWSERS.each do |browser|
  describe "#{browser}", :type => :feature, :driver => browser do
    
    SUPPORTED_WIDTHS.each do |width|
      describe "at #{width}px width" do
        before(:each) do
         page.driver.browser.manage.window.resize_to(width,1000)
        end
        
        it "should render the search page" do
          visit '/documents'
        end

        it "should render UK Butterfly Monitoring Scheme metadata record" do
          visit '/documents/571a676f-6c32-489b-b7ec-18dcc617a9f1'
        end
      end
    end

    after(:each) do |x|
      page.save_screenshot("rspec_screenshots/#{x.metadata[:full_description]}.png")
    end
  end
end