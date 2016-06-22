describe "Map Server Generation" do
  shared_examples "a renderable map source" do
    it "should be able to generate tms image" do
      resp = RestClient.get "#{APP_HOST}/documents/#{id}/onlineResources/0/tms/1.0.0/#{layer}/5/15/21.png", { :accept => 'image/png' }
      expect(resp.code).to eq 200
    end

    it "should be able to generate legend" do
      resp = RestClient.get "#{APP_HOST}/documents/#{id}/wms?REQUEST=GetLegendInfo&LAYER=#{layer}&SERVICE=WMS&FORMAT=image/png", { :accept => 'image/png' }
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
end
