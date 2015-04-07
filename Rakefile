require 'yaml'
require 'parallel'
require 'headless'

TEST_GROUPS = {
  :chrome_spec  => 'driver:chrome',
  #:firefox_spec => 'driver:firefox',
  #:ie_spec      => 'driver:ie_server',
  #:nexus5_spec  => 'driver:"Nexus 5"',
  #:nexus7_spec  => 'driver:"Nexus 7"',
  #:ipad2_spec   => 'driver:ipad2',
  :rest_spec    => 'restful'
}

task :default => [:grab_ids, :headless, :parallel_spec]

desc 'Set up Xfvb so that we can test in browsers headlessly'
task :headless do
  headless = Headless.new
  headless.start
  at_exit do
    headless.destroy
  end
end

desc 'Grab metadata ids from the vagrant box'
task :grab_ids do
  datastore = '/var/ceh-catalogue/datastore/'
  metadata = `vagrant ssh -c "ls #{datastore}*.raw"`
  names = metadata.split.map {|name| name[datastore.length..-5]}
  File.open('documents.yaml', 'w') {|f| f.write names.to_yaml }
end

desc 'Run each of the rspec groups in parallel'
task :parallel_spec do
  groups = TEST_GROUPS.keys

  Parallel.each(groups, :in_threads => groups.length) { |task|
    Rake::Task[task].execute
  }
end  

# Loop around each of the rspec tags and create a new task
TEST_GROUPS.each { |key, tag|
  desc "RSpec with specs tagged with #{tag}"
  task key do
    system "rspec --format progress --format RspecJunitFormatter --tag #{tag} --out test-reports/#{key}.xml"
  end
}
