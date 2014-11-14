describe "Metadata html generation", :retry => 1, :retry_wait => 0, :restful => true do
  # Grab a list of the current metadata stored inside the vagrant box
  datastore = '/var/ceh-catalogue/datastore/'
  metadata = `vagrant ssh -c "ls #{datastore}*.raw"`

  metadata.split.map {|name| name[datastore.length..-4]}.each {|id|
    it "should generate strictly valid html for #{id}" do
      res = Nokogiri::HTML( $site["/documents/#{id}"].get ) { |config| config.strict }
      expect(res.errors).to be_empty 
    end
  }
end