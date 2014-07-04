BROWSERS.each do |browser|
  describe "Metadata page in #{browser}", :type => :feature, :driver => browser do
    it "should show a dataset label on Countryside Survey Headwater Streams" do
      visit '/documents/16d7031f-78b0-47ea-b2bd-7dd1df487aa5'

      expect(page).to have_selector('.label-dataset')
    end

    it "should show a series label on UK Butterfly Monitoring Scheme (UKBMS) data" do
      visit '/documents/571a676f-6c32-489b-b7ec-18dcc617a9f1'

      expect(page).to have_selector('.label-series')
    end
  end
end
