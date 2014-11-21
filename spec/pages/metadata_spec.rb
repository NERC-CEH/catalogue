describe "Metadata page" do
  bmsPage = '/documents/571a676f-6c32-489b-b7ec-18dcc617a9f1'
  woodlandsPage = '/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae'
  starPage = '/documents/1d859249-e6af-48f4-9fd6-5f8401bc1e4e'
  buzzPage = '/documents/6795cf1b-9204-451e-99f1-f9b90658537f'
  radionuclideServicePage = '/documents/bb2d7874-7bf4-44de-aa43-348bd684a2fe'
  radionuclideDatasetPage = '/documents/fb495d1b-80a3-416b-8dc7-c85ed22ed1e3'
  notCurrentPage = '/documents/ff55462e-38a4-4f30-b562-f82ff263d9c3'

  sized [:xs, :sm, :md, :lg] do
    describe "UI elements of" do
    
      it "BMS page" do
        visit bmsPage
        expect(find('#document-title')).to have_content "UK Butterfly Monitoring Scheme (UKBMS) data"
        expect(find('#document-description')).to have_content "This is a series of datasets available from the UK Butterfly Monitoring Scheme"
        expect(find('#resource-type')).to have_content('series')
        expect(find('#section-spatial')).to have_content "OSGB 1936 / British National Grid"
        expect(find('.extentBegin')).to have_content "1976-04-01"
      end

      it "Woodlands page" do
        visit woodlandsPage
        expect(find('#keywords')).to have_content "biota"
        expect(first('.descriptive-keywords')).to have_content "NERC_DDC"
        expect(find('#document-authors')).to have_content "Kirby, K.J."
        expect(find('#document-order')).to have_link("Order/download", href: "http://gateway.ceh.ac.uk/download?fileIdentifier=2d023ce9-6dbe-4b4f-a0cd-34768e1455ae")
        expect(find('#document-order')).to have_link("Supporting documentation", href: "http://eidchub.ceh.ac.uk/metadata/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae")
        expect(find('#document-order')).not_to have_link("Preview on map")
      end

      it "it should not have a spatial reference heading" do
        visit starPage
        expect(page).not_to have_selector('#section-spatial')
      end
      
      it "should show not current message" do
        visit notCurrentPage
        expect(page).to have_selector('#not-current')
      end

    end

    describe "Get the data" do
      it "should show a Get the data panel with no links on The BUZZ project page" do
        visit buzzPage
        expect(page).not_to have_selector('#ordering-url')
      end
      
      it "should show a Preview on map link" do
        visit radionuclideServicePage
        expect(find('#document-order')).to have_link("Preview on map")
      end
    end

    describe "Document links on metadata page" do
      it "should have heading 'Services associated with this dataset:'" do
        visit radionuclideServicePage
        expect(page).to have_css('h4', :text=> 'Services associated with this dataset:')
        expect(page).to have_content("Natural radionuclide concentrations in soil, water and sediments in England and Wales survey maps")
      end

      it "should have heading 'Datasets associated with this service:'" do
        visit radionuclideDatasetPage
        expect(page).to have_css('h4',:text=> 'Datasets associated with this service:')
        expect(page).to have_content("Natural radionuclide concentrations in soil, water and sediments in England and Wales")
      end

      it "should not have heading 'Datasets associated with this service:'" do
        visit bmsPage
        expect(page).to_not have_css('h4',:text=> 'Datasets associated with this service:')
      end
    end
  end
end
