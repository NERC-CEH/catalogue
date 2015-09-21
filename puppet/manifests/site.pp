include apache
include apache::mod::proxy_ajp
include java8
include tomcat
include solr
include solr::jts

$config = hiera_hash('catalogue_config')
$configuration_location   = '/etc/ceh-catalogue'
$data_location            = '/var/ceh-catalogue'
$catalogue_config         = "${configuration_location}/catalogue.properties"
$tdb_location             = "${data_location}/tdb"
$catalogue_datarepository = "${data_location}/datastore"

# Link to the static content of the catalogue section of the project
file { '/var/www/static' :
  ensure => link,
  target => "/vagrant/catalogue/web/src",
}

# Create a location for data to be stored in for the catalogue
file { $data_location :
  ensure  => directory,
  mode    => '0644',
  owner   => $::tomcat::user,
  group   => $::tomcat::group,
  require => Class['::tomcat'],
}

file { $configuration_location :
  ensure  => directory,
  mode    => '0644',
  owner   => $::tomcat::user,
  group   => $::tomcat::group,
  require => Class['::tomcat'],
}

# Manage the catalogue configuration file
file { $catalogue_config : 
  mode    => '0644',
  content => inline_template("<%@config.each_pair do |k,v|%><%=k%>=<%=v%>\n<%end%>"),
  notify  => Service['tomcat7-ceh-catalogue'],
}

apache::vhost { 'https_gateway.ceh.ac.uk' :
  servername    => $fqdn,
  port          => '80',
  default_vhost => true,
  docroot       => '/var/www',
  proxy_pass   => [
    {path => '/static/', url => '!'},
    {path => '/id',      url => '!'},
    {path => '/',        url => "ajp://localhost:7100/"}
  ],
  redirect_source => ['/id'],
  redirect_dest   => ['/documents'],
  redirect_status => ['seeother'],
}

tomcat::instance { 'ceh-catalogue':
  ajp_port          => '7100',
  system_properties => {
    'config.file' => $catalogue_config
  },
}

tomcat::deployment { 'Deploy the CEH Catalogue' :
  war    => '/vagrant/catalogue/java/target/ROOT.war',
  tomcat => 'ceh-catalogue',
}
