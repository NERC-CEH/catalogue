describe "Search rest api", :retry => 1, :retry_wait => 0, :restful => true do
  it "should be able to find documents based on keyword" do
    resp = $site["/documents?term=infoMapAccessService"].get :accept => 'application/json'
    res = JSON.parse(resp)
    expect(res['results'].size).to be 1
    expect(res['results'][0]['identifier']).to eq 'fb495d1b-80a3-416b-8dc7-c85ed22ed1e3'
  end
end
