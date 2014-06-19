BROWSERS.each do |browser|
  describe "Search page in #{browser}", :type => :feature, :driver => browser do
    it "should find land cover map 2007 as top result" do
      visit "/documents"

      within("#search-form") do
        fill_in "term", :with => 'land cover map 2007'
        click_button "Search"
      end

      expect(first('.result')).to have_content 'Land Cover Map 2007'
    end

    it "should show the search term in the search box after a search" do
      visit "/documents"

      within("#search-form") do
        fill_in "term", :with => 'any old search term'
        click_button "Search"
      end

      expect(find_field('term').value).to eq 'any old search term'
    end
  end
end
