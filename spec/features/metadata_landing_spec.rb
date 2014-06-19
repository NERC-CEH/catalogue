BROWSERS.each do |browser|
	describe "Metadata landing page in #{browser}", :type => :feature, :driver => browser do
	  it "should work" do
		visit "/documents"
	  end
	end
end