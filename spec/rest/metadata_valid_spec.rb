describe "Metadata html generation", :retry => 1, :retry_wait => 0, :restful => true do
  GEMINI_IDS.each {|id|
    it "should generate strictly valid html for #{id}" do
      begin
        doc = $site["/documents/#{id}"].get :Remote_User => 'bamboo', :accept => 'text/html'
      rescue => e
        doc = e.response
      end
      res = Nokogiri::HTML( doc, nil, 'UTF-8' ) { |config| config.strict }
      expect(res.errors).to be_empty
    end
  }
end
