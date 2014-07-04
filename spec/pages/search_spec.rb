BROWSERS.each do |browser|
  
  def perform_search(term) 
      within("#search-form") do
        fill_in "term", :with => term
        click_button "Search"
      end
  end

  describe "Search page in #{browser}", :type => :feature, :driver => browser do
    it "should find land cover map 2007 as top result" do
      visit "/documents"

      perform_search 'land cover map 2007'

      expect(first('.result')).to have_content 'Land Cover Map 2007'
    end

    it "should show the search term in the search box after a search" do
      visit "/documents"

      perform_search 'any old search term'

      expect(find_field('term').value).to eq 'any old search term'
    end

    it "should show the correct label for the resource type" do
      visit "/documents"

      perform_search 'OS OnDemand Web Map Service'

      expect(first('.result')).to have_content 'OS OnDemand Web Map Service'
      expect(first('.result .label')).to have_content 'service'
    end

    it "should have dataset results with the label-dataset class applied" do
      visit "/documents"

      perform_search 'Land Cover Map 2007 vector'

      expect(first('.result')).to have_selector 'span.label-dataset'
    end

    it "should not find the facet 'application'" do
      visit "/documents"

      perform_search 'herbicide'

      facets = all('.facet-result-name')
        .select{ |e| e.text == 'application' }
        .map{ |e| e.text }

      expect(facets).to be_empty
    end

    it "should find the facet 'service'" do
      visit "/documents"

      perform_search 'herbicide'

      facets = all('.facet-result-name')
        .select{ |e| e.text == 'service' }
        .map{ |e| e.text }

      expect(facets).to match_array ['service']
    end

    it "should have search results when no search term" do
      visit "/documents"

      expect(all('.search-result-title')).not_to be_empty
    end

    it "should have different search results on two different visits when there is no search term" do
      visit "/documents"
      first_visit = all('.search-result-title').map{ |e| e.text }
      visit "/documents"
      second_visit = all('.search-result-title').map{ |e| e.text }

      expect(first_visit).not_to match_array second_visit
    end

  end

end
