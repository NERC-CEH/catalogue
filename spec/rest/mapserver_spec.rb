describe "Map Server Generation" do
  shared_examples "a renderable map source" do
    it "should be able to generate legend" do
      resp = RestClient.get "#{APP_HOST}/maps/#{id}?REQUEST=GetLegendInfo&LAYER=#{layer}&SERVICE=WMS&FORMAT=image/png", { :accept => 'image/png' }
      expect(resp.code).to eq 200
    end
  end

  context "shapefile data source" do
    let(:id) { 'mapserver-shapefile' }
    let(:layer) { 'ukdata' }
    it_behaves_like "a renderable map source"
  end

  context "raster data source" do
    let(:id) { 'mapserver-raster' }
    let(:layer) { 'Band1' }
    it_behaves_like "a renderable map source"
  end

  context "filegdb data source" do
    let(:id) { 'mapserver-file-gdb' }
    let(:layer) { 'FileGDB.Layer' }
    it_behaves_like "a renderable map source"
  end
end
