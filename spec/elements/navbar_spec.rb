xdescribe "The navbar" do
  describe "toggle button" do
    sized [:xs] do
      it "should be shown" do
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
      it "should be hidden" do
        visit '/documents'
        expect(page).to_not have_selector('.navbar-toggle')
      end
    end
  end
end
