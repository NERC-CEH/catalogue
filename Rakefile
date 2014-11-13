require 'parallel'
require 'headless'

TEST_GROUPS = {
  :chrome_spec  => 'driver:chrome',
  :firefox_spec => 'driver:firefox',
  :ie_spec      => 'driver:ie_server',
  :htc_spec     => 'driver:"HTC Desire X"',
  :nexus5_spec  => 'driver:"Nexus 5"',
  :nexus7_spec  => 'driver:"Nexus 7"',
  :ipad2_spec   => 'driver:ipad2',
  :rest_spec    => 'restful'
}

task :default do
  groups = TEST_GROUPS.keys

  # Start up in headless mode if the HEADLESS environment variable is defined
  if ENV['HEADLESS'] == 'true'
    headless = Headless.new
    headless.start
    at_exit do
      headless.destroy
    end
  end

  Parallel.each(groups, :in_threads => groups.length) { |task|
    Rake::Task[task].execute
  }
end

# Loop around each of the rspec tags and create a new task
TEST_GROUPS.each { |key, tag|
  task key do
    system "rspec --format progress --format RspecJunitFormatter --tag #{tag} --out #{task}_junit.xml"
  end
}
