describe "The navbar" do
  describe "toggle button" do
    sized [:xs] do
      
      it "should show when the width is 760px" do
        visit '/documents'
        expect(page).to have_selector('.navbar-toggle')
      end

      it "should expand when clicked" do
        visit '/documents'
        find('.navbar-toggle').click
        expect(page).to have_selector('.navbar-nav')
      end
    end

    sized [:md] do
      it "should be hidden when the width is 900px" do
        visit '/documents'
        expect(page).to_not have_selector('.navbar-toggle')
      end

      it "should be hidden by default" do
        visit '/documents'
        expect(page).to_not have_selector('.navbar-nav')
      end
    end
  end
end