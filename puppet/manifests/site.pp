include apache
include apache::mod::proxy_ajp
include java8
include tomcat
include solr
include solr::jts

create_resources('apache::vhost',      hiera_hash('apache::vhost', {}))
create_resources('tomcat::instance',   hiera_hash('tomcat::instance', {}))
create_resources('tomcat::deployment', hiera_hash('tomcat::deployment', {}))

# Link to the static content of the catalogue section of the project
file { '/var/www/static' :
  ensure => link,
  target => "/opt/ceh-catalogue/catalogue/web/src",
}

# Create a location for data to be stored in for the catalogue
file { '/var/ceh-catalogue' :
  ensure  => directory,
  mode    => '0644',
  owner   => $::tomcat::user,
  group   => $::tomcat::group,
  require => Class['::tomcat'],
}

tomcat::instance { 'ceh-catalogue':
  ajp_port          => hiera('catalogue_ajp_port'),
  system_properties => hiera_hash('catalogue_config'),
}
