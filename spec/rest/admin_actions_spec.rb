xdescribe "EIDC Pubisher" do

  it "should be able to retieve current publication state" do
    resp = RestClient.get "#{APP_HOST}/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae/publication", { :accept => :json, :remote_user => 'eidc-publisher' }
    res = JSON.parse(resp)
    expect(res['id']).to eq 'published'
    expect(res['transitions'].size).to be 1
    expect(res['transitions'][0]['href']).to eq "#{APP_HOST}/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae/publication/qtak5r"
  end

  it "should be able to retract document from publised state" do
    resp = RestClient.get "#{APP_HOST}/documents/2d023ce9-6dbe-4b4f-a0cd-34768e1455ae/publication", { :accept => :json, :remote_user => 'eidc-publisher' }
    res = JSON.parse(resp)
    resp = RestClient.post res['transitions'][0]['href'], headers={ :accept => :json, 'Remote-User' => 'eidc-publisher' }
    res = JSON.parse(resp)
    expect(res['id']).to eq 'draft'
  end
end