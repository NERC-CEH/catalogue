describe "Gemini XML Generation", :retry => 1, :retry_wait => 0, :restful => true do
  let(:schema) { 
    xsd_path = 'schemas/gemini/gmx/gmx.xsd'
    xsddoc = Nokogiri::XML(File.read(xsd_path), xsd_path)
    Nokogiri::XML::Schema.from_document(xsddoc)
  }

  let(:in_xml) {Nokogiri::XML(File.open("fixtures/datastore/REV-1/#{id}.raw"))}

  let(:out_doc) { RestClient.get "#{APP_HOST}/documents/#{id}?format=gemini" }
  let(:out_xml) { Nokogiri::XML(out_doc) { |c| c.strict } }

  shared_examples "a valid gemini document" do
    it "should be strictly valid xml" do
      expect(out_xml.errors).to be_empty
    end
    it "should be valid gemini xml" do
      expect(schema.valid?(out_xml)).to be true
    end
  end

  context "processing radio nuculides document" do
    let(:id) { 'bb2d7874-7bf4-44de-aa43-348bd684a2fe' }

    it "should generate valid bounding box" do
      west = '//gmd:westBoundLongitude'
      east = '//gmd:eastBoundLongitude'
      south = '//gmd:soutBoundLatitude'
      north = '//gmd:northBoundLatitude'

      expect(in_xml.xpath(west)).to be_equivalent_to(out_xml.xpath(west))
      expect(in_xml.xpath(east)).to be_equivalent_to(out_xml.xpath(east))
      expect(in_xml.xpath(south)).to be_equivalent_to(out_xml.xpath(south))
      expect(in_xml.xpath(north)).to be_equivalent_to(out_xml.xpath(north))
    end

    it_behaves_like "a valid gemini document"
  end

  context "document without a starting temporal extent" do
    let(:id) { '986d3df3-d9bf-42eb-8e18-850b8d54f37b' }
    it_behaves_like "a valid gemini document"
  end
end
