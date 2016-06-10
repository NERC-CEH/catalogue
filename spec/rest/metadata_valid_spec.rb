describe "Metadata html generation" do
  GEMINI_IDS.each {|id|
    it "should generate strictly valid html for #{id}" do
      begin
        doc = RestClient.get "#{APP_HOST}/documents/#{id}", headers: { :Remote_User => 'bamboo', :accept => 'text/html' }
      rescue => e
        doc = e.response
      end
      res = Nokogiri::HTML( doc, nil, 'UTF-8' ) { |config| config.strict }
      expect(res.errors).to be_empty
    end
  }
end
