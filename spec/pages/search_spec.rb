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

    it "should have less results when a facetted search is performed" do
      visit "/documents"
      num_records_unfiltered = first('#num-records').text.to_i
      find('.facet-link-inactive', :text => 'dataset').click()
      num_records_filtered = first('#num-records').text.to_i
      expect(num_records_filtered).to be < num_records_unfiltered
    end

    it "should have less results when a search term also includes a facet filter" do
      visit "/documents?term=land"
      num_records_unfiltered = first('#num-records').text.to_i
      find('.facet-link-inactive', :text => 'service').click()
      num_records_filtered = first('#num-records').text.to_i
      expect(num_records_filtered).to be < num_records_unfiltered
    end

    it "should have correct class applied to an active facet filter" do
      visit "/documents"
      find('.facet-link-inactive', :text => 'dataset').click()
      active_facet = find('.facet-filter-active')
      expect(active_facet.text).to eq('dataset')
    end

    it "should have correct class applied when an active facet filter has been deselected" do
      visit "/documents"
      find('.facet-link-inactive', :text => 'dataset').click
      find('.facet-link-active', :text => 'dataset').click

      inactive_dataset_facet = all('.facet-filter-inactive')
       .select{ |e| e.text[0,9] == 'dataset ('}
       .map{ |e| e.text }

      expect(inactive_dataset_facet.size).to eq(1)
    end

  end

end
