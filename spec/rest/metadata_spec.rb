describe "Metadata page generated HTML" do
  describe "RDFa properties" do
    before(:each) do
      @res = Nokogiri::HTML(
        $site['/documents/1e7d5e08-9e24-471b-ae37-49b477f695e3'].get
      )
    end

    it "should define prefixes on the metadata element" do
      prefix = @res.css('#metadata')[0]['prefix']
      expect(prefix).to include('dc: http://purl.org/dc/terms/')
      expect(prefix).to include('dcat: http://www.w3.org/ns/dcat#')
      expect(prefix).to include('foaf: http://xmlns.com/foaf/0.1/')
      expect(prefix).to include('geo: http://www.w3.org/2003/01/geo/wgs84_pos#')
    end

    it "should have dc:title property on title element" do
      expect(@res.css('#document-title')[0]['property']).to include('dc:title')
    end

    it "should have dc:type property on resource-type element" do
      expect(@res.css('#resource-type')[0]['property']).to include('dc:type')
    end

    it "should have dc:identifier property" do
      identifier = @res.css('[property="dc:identifier"]')
      expect(identifier.length).to be 1
      expect(identifier[0].to_str).to eq('1e7d5e08-9e24-471b-ae37-49b477f695e3')
    end

    it "should have dc:bibliographicCitation property on citation element" do
      expect(@res.css('#citation-text')[0]['property']).to include('dc:bibliographicCitation')
    end

    it "should have dc:creator property on elements with author class" do
      @res.css('#document-contacts > .contact-text').each do |contact|
        expect(contact.attribute('property').to_str).to include('dc:creator')
      end
    end

    it "should have dc:publisher property on elements with distributor or resource provider class" do
      @res.css('div.document-contacts > div.distributor, div.document-contacts > div.resource-provider').each do |contact|
        expect(contact.attribute('property').to_str).to include('dc:publisher')
      end
    end

    it "should have dc:abstract property on document-description element" do
      expect(@res.css('#document-description')[0]['property']).to include('dc:abstract')
    end

    it "should have dc:spatial property on all extent elements" do
      @res.css('#extent > img').each do |extent|
        expect(extent.attribute('property').to_str).to include('dc:spatial')
      end
    end

    it "should have dc:subject property on keywords element" do
      expect(@res.css('#keywords span')[0]['property']).to include('dc:subject')
    end

    it "should have dcat:accessURL property on link" do
      ordering = @res.css('a[property="dcat:accessURL"]')
      expect(ordering.length).to be 1
    end

    it "should have dc:rights property on license" do
      rights = @res.css('#section-licence [property="dc:rights"]')
      expect(rights.length).to be 1
    end

    it "should have dc:temporal property on temporal extent element" do
      expect(@res.css('#temporal-extent')[0]['property']).to include('dc:temporal')
    end

    it "should have dc:start on extentBegin" do
      expect(@res.css('.extentBegin')[0]['property']).to include('dc:start')
    end

    it "should have dc:end on extentEnd" do
      expect(@res.css('.extentEnd')[0]['property']).to include('dc:end')
    end

    it "should have dc:spatial property on studyarea-map" do
      expect(@res.css('#studyarea-map')[0]['property']).to include('dc:spatial')
    end

    it "should have geo:Geometry property on studyarea-map" do
      expect(@res.css('#studyarea-map')[0]['property']).to include('geo:Geometry')
    end

    it "should have foaf:agent in document-authors" do
      agents = @res.css('#document-authors [property="foaf:Agent"]')
      expect(agents.length).to be > 1
    end

    it "should have foaf:agent in document-otherContacts" do
      agents = @res.css('#document-otherContacts [property="foaf:Agent"]')
      expect(agents.length).to be > 1
    end

    it "should have dcat:Distribution on distributorContact" do
      expect(@res.css('#distributorContact-detail')[0]['property']).to include('dcat:Distribution')
    end

    it "should have dcat:keyword in keywords" do
      keywords = @res.css('#keywords [property="dcat:keyword"]')
      expect(keywords.length).to be > 1
    end
  end
end
