describe "Metadata page generated HTML" do
  describe "RDFa properties" do
    before(:each) do
      @res = Nokogiri::HTML(
        $site['/documents/1e7d5e08-9e24-471b-ae37-49b477f695e3'].get
      )
    end

    it "should define prefixes on the metadata element" do
      expect(@res.css('#metadata')[0]['prefix']).to include('dc: http://purl.org/dc/terms/','dcat: http://www.w3.org/ns/dcat#')
    end

    it "should have dc:title property on title element" do
      expect(@res.css('#document-title')[0]['property']).to include('dc:title')
    end

    it "should have dc:type property on resource-type element" do
      expect(@res.css('#resource-type')[0]['property']).to include('dc:type')
    end

    it "should have dc:identifier property on identifier element" do
      expect(@res.css('#file-identifier')[0]['property']).to include('dc:identifier')
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

    it "should have dc:description property on document-description element" do
      expect(@res.css('#document-description')[0]['property']).to include('dc:description')
    end

    it "should have dc:abstract property on document-description element" do
      expect(@res.css('#document-description')[0]['property']).to include('dc:abstract')
    end

    it "should have dc:spatial property on all extent elements" do
      @res.css('#extent > img').each do |extent|
        expect(extent.attribute('property').to_str).to include('dc:spatial')
      end
    end

    it "should have dc:subject property on document-keywords element" do
      expect(@res.css('.descriptive-keywords > span')[0]['property']).to include('dc:subject')
    end

    it "should have dcat:accessURL property on ordering link" do
      expect(@res.css('#ordering-url')[0]['property']).to include('dcat:accessURL')
    end

    it "should have dc:rights property on license" do
      expect(@res.css('#license')[0]['property']).to include('dc:rights')
    end

    it "should have dc:temporal property on temporal extent element" do
      expect(@res.css('#temporal-extent')[0]['property']).to include('dc:temporal')
    end
  end
end