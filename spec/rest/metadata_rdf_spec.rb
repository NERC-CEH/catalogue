DCAT = RDF::Vocabulary.new("http://www.w3.org/ns/dcat#")
GEO = RDF::Vocabulary.new("http://www.opengis.net/ont/geosparql#")

describe "Metadata RDF properties", :retry => 1, :retry_wait => 0, :restful => true do
  let(:graph) {
    RDF::Graph.load("#{APP_HOST}/documents/1e7d5e08-9e24-471b-ae37-49b477f695e3?format=rdfxml", :format => :rdfxml, :verify_none => true)
  }

  let(:subject) { RDF::URI.new("#{APP_HOST}/id/1e7d5e08-9e24-471b-ae37-49b477f695e3") }


  it "should have dc:title" do
    title = graph.first_literal([subject, RDF::DC.title, nil])
    expect(title.value).to eq 'Land Cover Map 2007 (1km raster, percentage aggregate class, N.Ireland)'
  end

  it "should have dc:type" do
    type = graph.first_literal([subject, RDF::DC.type, nil])
    expect(type.value).to eq 'Dataset'
  end

  it "should have dc:abstract" do
    abstract = graph.first_literal([subject, RDF::DC.abstract, nil])
    expect(abstract.value).to include 'parcel-based thematic'
  end

  it "should have one dcat:distribution" do
    distributions = graph.query [subject, DCAT.Distribution, nil]
    expect(distributions.count).to be 1
  end

  describe "Distribution" do
    let(:distribution) {graph.first_object([subject, DCAT.Distribution, nil])}

    it "should have accessURL" do
      url = graph.first_object [distribution, DCAT.accessURL, nil]
      expect(url.value).to include '.ceh.ac.uk/download?fileIdentifier=1e7d5e08-9e24-471b-ae37-49b477f695e3'
    end

    it "should have format" do
      format = graph.first_literal [distribution, RDF::DC.format, nil]
      expect(format.value).to eq 'geo-referenced TIFF image'
    end

    it "should have rights" do
      rights = graph.first_literal [distribution, RDF::DC.rights, nil]
      expect(rights.value).to include 'Refer to:'
    end
  end

  it "should have dc:bibliographicCitation" do
    bibliographicCitation = graph.first_literal([subject, RDF::DC.bibliographicCitation, nil])

    expect(bibliographicCitation.value).to include 'Morton'
  end

  it "should have dc:spatial" do
    spatial = graph.first_literal([subject, RDF::DC.spatial, nil])

    expect(spatial.value).to eq 'POLYGON((-8.21 53.68, -8.21 55.77, -5.26 55.77, -5.26 53.68, -8.21 53.68))'
    expect(spatial.datatype).to eq GEO.wktLiteral
  end

  it "should have dc:temporal" do
    temporal = graph.first_literal [subject, RDF::DC.temporal, nil]
    expect(temporal.value).to eq '2005-09-02/2008-07-18'
    expect(temporal.datatype).to eq RDF::DC.PeriodOfTime
  end

  it "should have two themes" do
    themes = graph.query [subject, DCAT.theme, nil]
    expect(themes.count).to be 2
    expect(themes.has_object? RDF::Literal.new('environment')).to be true
    expect(themes.has_object? RDF::Literal.new('imageryBaseMapsEarthCover')).to be true
  end

  it "should have dc:identifier property" do
    id = graph.first_literal [subject, RDF::DC.identifier, nil]
    expect(id.value).to eq '1e7d5e08-9e24-471b-ae37-49b477f695e3'
  end

  describe "dc:publisher" do
    let(:publisher) {graph.first([subject, RDF::DC.publisher, nil]).object}

    it "has vcard organization-name" do
      name = graph.first_literal([publisher, RDF::VCARD['organization-name'], nil ])
      expect(name.value).to include 'Centre for Ecology & Hydrology'
    end

    describe "v:adr" do
      let(:address) {graph.first([publisher, RDF::VCARD.adr, nil]).object}
    
      it "has street-address" do
        address = graph.first_literal([address, RDF::VCARD['street-address'], nil ])
        expect(address.value).to include 'Bailrigg'
      end

      it "has locality" do
        locality = graph.first_literal([address, RDF::VCARD.locality, nil ])
        expect(locality.value).to eq 'Lancaster'
      end

      it "has region" do
        region = graph.first_literal([address, RDF::VCARD.region, nil ])
        expect(region.value).to eq 'Lancashire'
      end

      it "has postal-code" do
        postCode = graph.first_literal([address, RDF::VCARD['postal-code'], nil ])
        expect(postCode.value).to eq 'LA1 4AP'
      end

      it "has country-name" do
        country = graph.first_literal([address, RDF::VCARD['country-name'], nil ])
        expect(country.value).to eq 'United Kingdom'
      end
    end
  end
end
