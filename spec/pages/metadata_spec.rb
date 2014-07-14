BROWSERS.each do |browser|
  describe "Metadata page in #{browser}", :type => :feature, :driver => browser do
    describe "resourceType label" do
      it "should show a dataset label on Countryside Survey Headwater Streams" do
        visit '/documents/16d7031f-78b0-47ea-b2bd-7dd1df487aa5'

        expect(page).to have_selector('.label-dataset')
      end

      it "should show a series label on UK Butterfly Monitoring Scheme (UKBMS) data" do
        visit '/documents/571a676f-6c32-489b-b7ec-18dcc617a9f1'
        expect(page).to have_selector('.label-series')
      end
    end

    describe "UI on metadata page" do

      it "should show the correct title for the UK Butterfly Monitoring Scheme (UKBMS) data record" do
        visit '/documents/571a676f-6c32-489b-b7ec-18dcc617a9f1'
        expect(first('#document-title')).to have_content "UK Butterfly Monitoring Scheme (UKBMS) data"
        puts first('#document-title').text
      end

      it "should show the correct description for the UK Butterfly Monitoring Scheme (UKBMS) data record" do
        visit '/documents/571a676f-6c32-489b-b7ec-18dcc617a9f1'
        expect(first('.document-description')).to have_content "This is a series of datasets available from the UK Butterfly Monitoring Scheme (UKBMS). The UKBMS comprises a suite of surveys carried out each year across the UK that generate data on butterfly abundance. The UKBMS started in 1976 (few sites from 1973) with thirty-six sites increasing to over a thousand by 2010. On the majority of these sites, recorders walk a fixed route (divided into up to 15 sections) at their site every week from 1st April to 29th September (total of 26 weeks), each year, counting butterflies within defined limits (5m box) when weather meets certain criteria. On some sites different survey techniques are employed to assess the abundance of species. These techniques include timed counts and larval web surveys in fixed areas. Weather data is recorded at the time of the count and habitat data is available for most transects. The surveys are undertaken by volunteers and other recorders, who contribute their data free of charge. The scheme is operated as a partnership between the Centre For Ecology and Hydrology (CEH) and Butterfly Conservation (BC), and is jointly sponsored by a multi-agency consortium of the Joint Nature Conservation Committee (JNCC), Natural Environment Research Council (through CEH), BC, Department of the Environment Food and Rural Affairs, the Countryside Council for Wales, Natural England, Northern Ireland Environment Agency, Forestry Commission and Scottish Natural Heritage. UKBMS data is widely used for research; a list of publications as well as downloadable PDF copies of past Annual Reports is made available via the UKBMS website. Datasets currently available for download are yearly calculations of species trends and collated indices of abundance, as well as yearly updated site data. Access to the full dataset should be requested directly from the UKBMS."
      end

      it "should show the correct topic categories for the Woodlands survey flora data 1971-2001" do
        visit '/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae'
        topics = all('.document-categories')
          .map{ |e| e.text }
        expect(topics).to match_array ["biota environment"]
      end

      it "should show the correct keywords for the Woodlands survey flora data 1971-2001" do
        visit '/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae'
        keywords = all('.document-keywords')
          .map{ |e| e.text }
        expect(keywords).to match_array ["Environmental monitoring facilities woodland trees plants Britain NERC_DDC"]
      end

      it "should show the correct contacts for the Woodlands survey flora data 1971-2001" do
        visit '/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae'
        contacts = all('.document-contacts')
          .map{ |e| e.text }
        expect(contacts).to match_array ["Author Black, H.I.J. Centre for Ecology & Hydrology enquiries@ceh.ac.uk", "Author Bunce, R.G.H. Alterra enquiries@ceh.ac.uk", "Author Corney, P.M. University of Liverpool enquiries@ceh.ac.uk", "Author Kirby, K.J. English Nature enquiries@ceh.ac.uk", "Author Shaw, M.W. The Nature Conservancy enquiries@ceh.ac.uk", "Author Smart, S.M. Centre for Ecology & Hydrology enquiries@ceh.ac.uk", "Author Smithers, R.J. Woodland Trust enquiries@ceh.ac.uk", "Custodian EIDC Hub eidc@ceh.ac.uk", "Point Of Contact Centre for Ecology & Hydrology Lancaster Environment Centre, Library Avenue, Bailrigg Lancashire Lancaster United Kingdom LA1 4AP enquiries@ceh.ac.uk", "Point Of Contact Centre for Ecology & Hydrology Lancaster Environment Centre, Library Avenue, Bailrigg Lancashire Lancaster United Kingdom LA1 4AP enquiries@ceh.ac.uk", "Point Of Contact Centre for Ecology & Hydrology Lancaster Environment Centre, Library Avenue, Bailrigg Lancashire Lancaster United Kingdom LA1 4AP enquries@ceh.ac.uk", "Resource Provider Parr Section enquiries@ceh.ac.uk"]
      end
    end

    describe "Get the data panel" do

      it "should show a Get the data panel on Woodlands survey flora data 1971-2001 with links" do
        visit '/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae'

        expect(page).to have_selector('.document-ordering')
        expect(page).to have_link("Order/Download", href: "http://gateway.ceh.ac.uk/download?fileIdentifier=2d023ce9-6dbe-4b4f-a0cd-34768e1455ae")
        expect(page).to have_link("Supporting documentation", href: "http://eidchub.ceh.ac.uk/metadata/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae")
        expect(page).to have_link("This resource is available under the Open Government Licence (OGL)", href: "http://eidchub.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/ceh-open-government-licence/plain")
      end

      it "should show a Get the data panel with no links on The BUZZ project: comparison of new and existing agri-environment scheme options for biodiversity enhancement on arable land" do
        visit '/documents/6795cf1b-9204-451e-99f1-f9b90658537f'

        expect(page).to have_selector('.document-ordering')
        expect(first('.document-ordering')).to have_content "No Order/Download available"
        expect(first('.document-ordering')).to have_content "Licence not found"
      end
    end
    
    describe "Document links on metadata page" do
      it "should have heading 'Associated Services'" do
        visit '/documents/bb2d7874-7bf4-44de-aa43-348bd684a2fe' 
        
        expect(page).to have_content('Associated Services')
      end
      
      it "should have link to service" do
        visit '/documents/bb2d7874-7bf4-44de-aa43-348bd684a2fe' 
        
        expect(page).to have_content("Natural radionuclide concentrations in soil, water and sediments in England and Wales survey maps")
      end
      
      it "should have heading 'Associated Datasets'" do
        visit '/documents/fb495d1b-80a3-416b-8dc7-c85ed22ed1e3' 
        
        expect(page).to have_content('Associated Datasets')
      end
      
      it "should have link to dataset" do
        visit '/documents/fb495d1b-80a3-416b-8dc7-c85ed22ed1e3' 
        
        expect(page).to have_content("Natural radionuclide concentrations in soil, water and sediments in England and Wales")
      end
      
      it "should not have heading 'Associated Datasets'" do
        visit '/documents/88657668-1f28-4b72-9609-e993235c9428' 
        
        expect(page).not_to have_content('Associated Datasets')
      end
    end

  end
end