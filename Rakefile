require 'rspec/core/rake_task'
require 'parallel'

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

  Parallel.each(groups, :in_processes => groups.length) { |task|
    Rake::Task[task].execute
  }
end

TEST_GROUPS.each { |task, tag|
  RSpec::Core::RakeTask.new(task) do |t|
    t.rspec_opts = "--format RspecJunitFormatter --tag #{tag} --out #{task}_junit.xml"
  end
}
