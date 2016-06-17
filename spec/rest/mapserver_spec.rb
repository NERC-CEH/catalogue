describe "Map Server Generation" do
  it "should be able to generate tms image" do
    resp = RestClient.get "#{APP_HOST}/documents/mapserverService/onlineResources/0/tms/1.0.0/ukdata/5/15/21.png", { :accept => 'image/png' }
    expect(resp.code).to eq 200
  end

  it "should be able to generate legend" do
    resp = RestClient.get "#{APP_HOST}/documents/mapserverService/wms?REQUEST=GetLegendInfo&LAYER=ukdata&SERVICE=WMS&FORMAT=image/png", { :accept => 'image/png' }
    expect(resp.code).to eq 200
  end
end
