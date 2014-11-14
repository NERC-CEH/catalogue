describe "Metadata RDFa properties", :retry => 1, :retry_wait => 0, :restful => true do
  before(:each) do
    @res = Nokogiri::HTML(
      $site['/documents/1e7d5e08-9e24-471b-ae37-49b477f695e3'].get
    )
  end

  it "should define prefixes on the metadata element" do
    prefix = @res.css('#metadata')[0]['prefix']
    expect(prefix).to include('dc: http://purl.org/dc/terms/')
    expect(prefix).to include('geo: http://www.opengis.net/ont/geosparql#')
    expect(prefix).to include('v: http://www.w3.org/2006/vcard/ns#')
  end

  it "should have dc:title property on title element" do
    title = @res.css('#document-title')[0]
    
    expect(title['property']).to eq 'dc:title'
    expect(title['content']).to eq 'Land Cover Map 2007 (1km raster, percentage aggregate class, N.Ireland)'
  end

  it "should have two dcat:themes" do
    themes = @res.css('[property="dcat:theme"]')
    expect(themes.length).to be 2
    expect(themes[0]['content']).to eq 'environment'
    expect(themes[1]['content']).to eq 'imageryBaseMapsEarthCover'
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

  it "should have dcat:accessURL property on link" do
    ordering = @res.css('a[property="dcat:accessURL"]')
    expect(ordering.length).to be 1
  end

  it "should have dc:rights property" do
    rights = @res.css('[property="dc:rights"]')
    expect(rights.length).to be 1
  end

  it "should have dc:temporal property on temporal extent element" do
    expect(@res.css('#temporal-extent')[0]['property']).to include('dc:temporal')
  end

  it "should have dc:spatial property on studyarea-map" do
    extents = @res.css('#studyarea-map [property="dc:spatial"]')
    expect(extents[0]['content']).to include 'POLYGON(('
    expect(extents[0]['datatype']).to eq 'geo:wktLiteral'
  end

  it "should have single foaf:agent in section-metadata" do
    agents = @res.css('[rel="foaf:Agent"]')
    expect(agents.length).to be 1
    expect(@res.css('#section-metadata [rel="foaf:Agent"]')).to eq agents
  end

  it "should have dcat:Distribution" do
    expect(@res.css('[property="dcat:Distribution"]').length).to be 1
  end

  it "should have dcat:keyword in keywords" do
    keywords = @res.css('#keywords [property="dcat:keyword"]')
    expect(keywords.length).to be > 1
  end
end
