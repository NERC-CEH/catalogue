BROWSERS.each do |browser|
  describe "#{browser} rendering the navbar", :type => :feature, :driver => browser do
    describe "toggle button" do
      it "should show when the width is 760px" do
        page.driver.browser.manage.window.resize_to(760,400)

        visit '/documents'

        expect(page).to have_selector('.navbar-toggle')
      end

      it "should be hidden when the width is 900px" do
        page.driver.browser.manage.window.resize_to(900,400)

        visit '/documents'

        expect(page).to_not have_selector('.navbar-toggle')
      end

      it "should expand when clicked" do
        page.driver.browser.manage.window.resize_to(760,400)

        visit '/documents'
        find('.navbar-toggle').click

        expect(page).to have_selector('.navbar-nav')
      end

      it "should be hidden by default" do
        page.driver.browser.manage.window.resize_to(760,400)

        visit '/documents'

        expect(page).to_not have_selector('.navbar-nav')
      end
    end
  end
end