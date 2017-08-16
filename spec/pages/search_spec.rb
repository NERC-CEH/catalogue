describe "Search page" do
  sized [:xs, :sm, :md, :lg] do
    it 'should update results when term changes' do
      visit "/eidc/documents"
      results = first('.result').text
      within(".search-form") do
        fill_in 'term', :with => 'my search'
      end
      expect(page).not_to have_content results
    end

    it 'should update results when going to next page' do
      visit "/eidc/documents?rows=2"
      results = first('.result').text
      click_on 'Next'
      expect(page).not_to have_content results
    end

    it 'should find record when doing case-insesitive search for pollution keyword' do
      visit '/eidc/documents?term=keyword%3Apollution'
      result = first('.result .title').text
      expect(result).to have_content 'Natural radionuclide concentrations in soil, water and sediments in England and Wales'
    end
  end

  after(:each) do |x|
    page.save_screenshot("test-reports/#{x.metadata[:full_description]}.png")
  end
end
