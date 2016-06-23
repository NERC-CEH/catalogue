describe "Map Server Generation" do
  shared_examples "a renderable map source" do
    it "should be able to generate tms image" do
      resp = RestClient.get "#{APP_HOST}/documents/#{id}/onlineResources/0/tms/1.0.0/#{layer}/3/3/5.png", :accept => 'image/png'
      expect(resp.code).to eq 200
    end

    it "should be able to generate tms legend" do
      resp = RestClient.get "#{APP_HOST}/documents/#{id}/onlineResources/0/legend", :accept => 'image/png'
      expect(resp.code).to eq 200
    end

    it "should be able to generate wms legend" do
      resp = RestClient.get "#{APP_HOST}/maps/#{id}", :accept => 'image/png', :params => {
        :SERVICE => 'WMS',
        :REQUEST => 'GetLegendInfo',
        :VERSION => '1.1.1',
        :LAYERS  => layer,
        :FORMAT  => 'image/png'
      }
      expect(resp.code).to eq 200
    end

    it "should be able to generate wms map" do
      resp = RestClient.get "#{APP_HOST}/maps/#{id}?", :accept => 'image/png', :params => {
        :SERVICE => 'WMS',
        :REQUEST => 'GetMap',
        :VERSION => '1.1.1',
        :LAYERS  => layer,
        :STYLES  => '',
        :FORMAT  => 'image/png',
        :HEIGHT  => 256,
        :WIDTH   => 256,
        :SRS     =>'EPSG:27700',
        :BBOX    => '0,0,700000,1300000'
      }
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
