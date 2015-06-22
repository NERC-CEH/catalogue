describe "Gemini XML Generation", :retry => 1, :retry_wait => 0, :restful => true do
  let(:schema) { 
    xsd_path = 'fixtures/gemini-schema/gmx/gmx.xsd'
    xsddoc = Nokogiri::XML(File.read(xsd_path), xsd_path)
    Nokogiri::XML::Schema.from_document(xsddoc)
  }

  let(:id) { 'bb2d7874-7bf4-44de-aa43-348bd684a2fe' }
  let(:in_xml) {Nokogiri::XML(File.open("fixtures/REV-1/#{id}.raw"))}

  let(:out_doc) { $site["/documents/#{id}?format=gemini"].get }
  let(:out_xml) { Nokogiri::XML(out_doc) { |c| c.strict } }

  it "should be strictly valid xml" do
    expect(out_xml.errors).to be_empty
  end

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

  it "should be valid gemini xml" do
    expect(schema.valid?(out_xml)).to be true
  end
end
