describe "Metadata html generation", :retry => 1, :retry_wait => 0, :restful => true do 
  it "should generate strictly valid html for all documents" do
    # Grab a list of the current metadata stored inside the vagrant box
    datastore = '/var/ceh-catalogue/datastore/'
    metadata = `vagrant ssh -c "ls #{datastore}*.raw"`

    metadata.split.map {|name| name[datastore.length..-5]}.each {|id|
      doc = $site["/documents/#{id}"].get
      res = Nokogiri::HTML( doc, nil, 'UTF-8' ) { |config| config.strict }
      expect(res.errors).to be_empty 
    }
  end
end
