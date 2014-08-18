BROWSERS.each do |browser|
  bmsPage = '/documents/571a676f-6c32-489b-b7ec-18dcc617a9f1'
  woodlandsPage = '/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae'
  starPage = '/documents/1d859249-e6af-48f4-9fd6-5f8401bc1e4e'
  buzzPage = '/documents/6795cf1b-9204-451e-99f1-f9b90658537f'
  radionuclideServicePage = '/documents/bb2d7874-7bf4-44de-aa43-348bd684a2fe'
  radionuclideDatasetPage = '/documents/fb495d1b-80a3-416b-8dc7-c85ed22ed1e3'
  notCurrentPage = '/documents/ff55462e-38a4-4f30-b562-f82ff263d9c3'
  
  describe "Metadata page in #{browser}", :type => :feature, :driver => browser do

    describe "UI elements of" do
    
      it "BMS page" do
        visit bmsPage
        expect(find('#document-title')).to have_content "UK Butterfly Monitoring Scheme (UKBMS) data"
        expect(find('#document-description')).to have_content "This is a series of datasets available from the UK Butterfly Monitoring Scheme"
        expect(find('#resource-type')).to have_content('Series')
        expect(find('#resource-status')).to have_content "onGoing"
        expect(find('#spatial-reference-system')).to have_content "OSGB 1936 / British National Grid"
        expect(find('.extentBegin')).to have_content "1976-04-01"
      end

      it "Woodlands page" do
        visit woodlandsPage
        expect(find('#resource-status')).to have_content "completed"
        expect(find('#topic-categories')).to have_content "biota, environment"
        expect(first('.descriptive-keywords')).to have_content "NERC_DDC"
        expect(find('#document-contacts').first('.contact-text')).to have_content "Author Kirby, K.J. English Nature enquiries@ceh.ac.uk"
        expect(page).to have_selector('#browse-graphic')
        expect(page).to have_selector('#document-ordering')
        expect(find('#document-ordering')).to have_link("Order/Download", href: "http://gateway.ceh.ac.uk/download?fileIdentifier=2d023ce9-6dbe-4b4f-a0cd-34768e1455ae")
        expect(find('#document-ordering')).to have_link("Supporting documentation", href: "http://eidchub.ceh.ac.uk/metadata/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae")
        expect(find('#document-ordering')).to have_link("Resource available under an Open Government Licence", href: "http://eidchub.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/ceh-open-government-licence/plain")
      end

      it "it should not have a spatial reference heading" do
        visit starPage
        expect(page).not_to have_selector('#spatial-reference-system')
      end
      
      it "should show not current message" do
        visit notCurrentPage
        expect(page).to have_selector('#not-current')
      end

    end

    describe "Get the data" do
      it "should show a Get the data panel with no links on The BUZZ project page" do
        visit buzzPage
        expect(page).not_to have_selector('#document-ordering')
      end
    end

    describe "Document links on metadata page" do
      it "should have heading 'Associated Services'" do
        visit radionuclideServicePage
        expect(find('#document-additional-info')).to have_content('Associated Services')
        expect(page).to have_selector('#document-links')
      end

      it "should have link to service" do
        visit radionuclideServicePage
        expect(find('#document-links')).to have_content("Natural radionuclide concentrations in soil, water and sediments in England and Wales survey maps")
      end

      it "should have heading 'Associated Datasets'" do
        visit radionuclideDatasetPage
        expect(find('#document-additional-info')).to have_content('Associated Datasets')
      end

      it "should have link to dataset" do
        visit radionuclideDatasetPage
        expect(find('#document-links')).to have_content("Natural radionuclide concentrations in soil, water and sediments in England and Wales")
      end

      it "should not have heading 'Associated Datasets'" do
        visit bmsPage
        expect(find('#document-additional-info')).not_to have_content('Associated Datasets')
        expect(find('#document-additional-info')).not_to have_selector('#document-links')
      end
    end
  end
end