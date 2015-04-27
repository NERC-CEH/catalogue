describe "Search page" do
  sized [:xs, :sm, :md, :lg] do
    it 'should update results when term changes' do
      visit "/documents?rows=2"
      results = first('.result').text
      within(".search-form") do
        fill_in 'term', :with => 'my search'
      end
      expect(page).not_to have_content results
    end

    it 'should update results when going to next page' do
      visit "/documents?rows=2"
      results = first('.result').text
      click_on 'Next'
      expect(page).not_to have_content results
    end

    it 'should update results when going to previous page' do
      visit '/documents?rows=2&page=2'
      results = first('.result').text
      click_on 'Previous'
      expect(page).not_to have_content results
    end
  end
end
