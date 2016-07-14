describe "Map Server Generation" do
  shared_examples "a wms" do |content_type|
    it "should respond" do
      resp = RestClient.get "#{APP_HOST}/maps/#{id}?", :accept => 'image/png', :params => {
        :SERVICE => 'WMS',
        :REQUEST => 'GetMap',
        :VERSION => '1.1.1',
        :LAYERS  => layer,
        :STYLES  => '',
        :FORMAT  => 'image/png',
        :HEIGHT  => 256,
        :WIDTH   => 256,
        :SRS     => projection,
        :BBOX    => '0,0,700000,1300000'
      }
      expect(resp.code).to eq 200
      expect(resp.headers[:content_type]).to eq content_type
    end
  end

  shared_examples "a renderable map source" do
    it "should be able to generate tms image" do
      resp = RestClient.get "#{APP_HOST}/documents/#{id}/onlineResources/0/tms/1.0.0/#{layer}/3/3/5.png", :accept => 'image/png'
      expect(resp.code).to eq 200
    end

    it "should be able to generate tms legend" do
      resp = RestClient.get "#{APP_HOST}/documents/#{id}/onlineResources/0/#{layer}/legend", :accept => 'image/png'
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

    it_behaves_like "a wms", "image/png"
  end

  context "shapefile data source" do
    let(:id) { 'mapserver-shapefile' }
    let(:layer) { 'ukdata' }
    let(:projection) { 'EPSG:27700' }
    it_behaves_like "a renderable map source"
    
    it "should support GetFeatureInfo" do |feature_count|
      resp = RestClient.get "#{APP_HOST}/maps/#{id}?", :params => {
        :SERVICE      => 'WMS',
        :REQUEST      => 'GetFeatureInfo',
        :VERSION      => '1.3.0',
        :LAYERS       => layer,
        :QUERY_LAYERS => layer,
        :INFO_FORMAT  => 'application/vnd.ogc.gml',
        :HEIGHT       => 256,
        :WIDTH        => 256,
        :X            => 128,
        :Y            => 128,
        :CRS          => projection,
        :BBOX         => '0,0,700000,1300000'
      }
      xml = Nokogiri::XML(resp.body)
      expect(xml.css("#{layer}_feature").length).to eq 1
    end
  end

  context "raster data source" do
    let(:id) { 'mapserver-raster' }
    let(:layer) { 'Band1' }
    let(:projection) { 'EPSG:27700' }
    it_behaves_like "a renderable map source"
  end

  context "filegdb data source" do
    let(:id) { 'mapserver-file-gdb' }
    let(:layer) { 'FileGDB.Layer' }
    let(:projection) { 'EPSG:27700' }
    it_behaves_like "a renderable map source"
  end

  context "shapefile without attribute filtering" do
    let(:id) { 'mapserver-all-features' }
    let(:layer) { 'ukdata'}
    let(:projection) { 'EPSG:27700' }
    it_behaves_like "a renderable map source"
  end

  context "multiple-projection data source" do
    let(:id) { 'mapserver-multiple-projections' }
    let(:layer) { 'ukdata' }

    context "with broken projection" do
      let(:projection) { 'EPSG:3857' }
      it_behaves_like "a wms", "application/vnd.ogc.se_xml;charset=UTF-8"
    end

    context "with working projection" do
      let(:projection) { 'EPSG:27700' }
      it_behaves_like "a wms", "image/png"
    end
  end
end
