FROM ubuntu:14.04 
MAINTAINER oss@ceh.ac.uk

# Install puppet 
RUN apt-get -y update 
RUN apt-get -y install ruby1.9.1 ruby1.9.1-dev build-essential 
RUN echo "gem: --no-ri --no-rdoc" > ~/.gemrc 
RUN gem install puppet -v '3.7.5' 
RUN gem install librarian-puppet -v '2.2.1' 
RUN apt-get -y install lsb-release wget 

# Set up the supervisor 
RUN apt-get install -y supervisor 
RUN mkdir -p /var/log/supervisor 

# Configure the box 
ADD Puppetfile / 
RUN librarian-puppet install 
RUN mkdir -p /vagrant /opt/ceh-catalogue 
ADD /puppet/ /opt/ceh-catalogue/puppet 
ADD /catalogue /opt/ceh-catalogue/catalogue 

# TODO: Puppet will attempt to start the tomcat services which fail inside a container 
RUN puppet apply --modulepath=/modules --hiera_config=/opt/ceh-catalogue/puppet/hiera.yaml /opt/ceh-catalogue/puppet/manifests/site.pp 

# Configure the services 
COPY supervisord/catalogue.conf /etc/supervisor/conf.d/catalogue.conf 

# Expose the catalogue datastore 
VOLUME ["/var/ceh-catalogue/datastore"] 
EXPOSE 7000 80 

CMD ["/usr/bin/supervisord"] 
