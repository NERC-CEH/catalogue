DCAT = RDF::Vocabulary.new("http://www.w3.org/ns/dcat#")
GEO = RDF::Vocabulary.new("http://www.opengis.net/ont/geosparql#")
DCT = RDF::Vocabulary.new("http://purl.org/dc/terms/") 

describe "Metadata RDF properties", :retry => 1, :retry_wait => 0, :restful => true do
  let(:graph) {
    RDF::Graph.load("#{APP_HOST}/documents/1e7d5e08-9e24-471b-ae37-49b477f695e3?format=rdfxml", :format => :rdfxml, :verify_none => true)
  }

  let(:subject) { RDF::URI.new("#{APP_HOST}/id/1e7d5e08-9e24-471b-ae37-49b477f695e3") }

  it "should have dc:title" do
    title = graph.first([subject, RDF::DC.title, nil])
    expect(title.object).to eq 'Land Cover Map 2007 (1km raster, percentage aggregate class, N.Ireland)'
  end

  it "should have dct:type" do
    type = graph.first([subject, DCT.type, nil])
    expect(type.object).to eq RDF::URI.new('http://purl.org/dc/dcmitype/Dataset')
  end

  it "should have dc:abstract" do
    abstract = graph.first_literal([subject, DCT.description, nil])
    expect(abstract.value).to include 'parcel-based thematic'
  end

  it "should have one dcat:distribution" do
    distributions = graph.query [subject, DCAT.Distribution, nil]
    expect(distributions.count).to be 1
  end

  describe "Distribution" do
    let(:distribution) {graph.first_object([subject, DCAT.Distribution, nil])}

    it "should have accessURL" do
      url = graph.first [distribution, DCAT.accessURL, nil]
      expect(url.object).to eq RDF::URI.new('https://catalogue.ceh.ac.uk/download?fileIdentifier=1e7d5e08-9e24-471b-ae37-49b477f695e3')
    end

    it "should have format" do
      format = graph.first_object [distribution, DCT.format, nil]
      imt = graph.first_object [format, DCT.IMT, nil]
      value = graph.first_literal [imt, RDF.value, nil]
      expect(value).to eq 'geo-referenced TIFF image'
    end

    it "should have rights" do
      rights = graph.query [distribution, DCT.rights, nil]
      expect(rights.count).to be 2
      #expect(rights.has_object? RDF::Literal.new('If you reuse this data, you must cite Morton, R.D.,Rowland, C.,Wood, C.,Meek, L.,Marston, G.,Smith, G.,Wadsworth, R.,Simpson, I. (2011). Land Cover Map 2007 (1km raster, percentage aggregate class, N.Ireland). NERC Environmental Information Data Centre. http://doi.org/10.5285/1e7d5e08-9e24-471b-ae37-49b477f695e3')).to be true 
      #expect(rights.has_object? RDF::Literal.new('Refer to: R.D. Morton, C. Rowland, C. Wood, L. Meek, C. Marston, G. Smith, R. Wadsworth, I. Simpson. July 2011 CS Technical Report No 11/07: Final Report for LCM2007 - the new UK land cover map. NERC/Centre for Ecology &amp; Hydrology (CEH Project Number NEC03259).')).to be true 
    end
  end

  it "should have dc:bibliographicCitation" do
    bibliographicCitation = graph.first_literal([subject, RDF::DC.bibliographicCitation, nil])

    expect(bibliographicCitation.value).to include 'Morton'
  end

  it "should have dc:spatial" do
    spatial = graph.first_literal([subject, RDF::DC.spatial, nil])

    expect(spatial.value).to eq 'POLYGON((-8 54, -8 56, -5 56, -5 54, -8 54))'
    expect(spatial.datatype).to eq GEO.wktLiteral
  end

  it "should have dc:temporal" do
    temporal = graph.first_literal [subject, RDF::DC.temporal, nil]
    expect(temporal.value).to eq '2005-09-02/2008-07-18'
    expect(temporal.datatype).to eq RDF::DC.PeriodOfTime
  end

  it "should have two subjects" do
    subjects = graph.query [subject, DCT.subject, nil]
    expect(subjects.count).to be 2
    expect(subjects.has_object? RDF::URI.new('http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/environment')).to be true
    expect(subjects.has_object? RDF::URI.new('http://inspire.ec.europa.eu/metadata-codelist/TopicCategory/imageryBaseMapsEarthCover')).to be true
  end

  it "should have dc:identifier property" do
    id = graph.first([subject, DCT.identifier, nil])
    expect(id.object).to eq 'CEH:EIDC:1300193660704'
  end

  describe "dc:publisher" do
    let(:publisher) {graph.first([subject, RDF::DC.publisher, nil]).object}

    it "has vcard organization-name" do
      name = graph.first_literal([publisher, RDF::VCARD['organization-name'], nil ])
      expect(name.value).to include 'NERC Environmental Information Data Centre'
    end
    
    it "has vcard email" do
      name = graph.first_literal([publisher, RDF::VCARD['email'], nil ])
      expect(name.value).to include 'eidc@ceh.ac.uk'
    end
  end
end
