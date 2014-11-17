describe "Metadata html generation", :retry => 1, :retry_wait => 0, :restful => true do
  METADATA_IDS.each {|id|
    it "should generate strictly valid html for #{id}" do
      doc = $site["/documents/#{id}"].get
      res = Nokogiri::HTML( doc, nil, 'UTF-8' ) { |config| config.strict }
      expect(res.errors).to be_empty 
    end
  }
end
