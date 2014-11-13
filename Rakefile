require 'parallel'
require 'headless'

TEST_GROUPS = {
  :selenium_chrome  => 'driver:chrome',
  :selenium_firefox => 'driver:firefox',
  :selenium_ie      => 'driver:ie_server',
  :selenium_htc     => 'driver:"HTC Desire X"',
  :selenium_nexus5  => 'driver:"Nexus 5"',
  :selenium_nexus7  => 'driver:"Nexus 7"',
  :selenium_ipad2   => 'driver:ipad2',
  :rest             => 'restful'
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

TEST_GROUPS.each { |key, tag|
  task key do
    system "rspec --format progress --format RspecJunitFormatter --tag #{tag} --out #{task}_junit.xml"
  end
}
