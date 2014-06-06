# Stupid windows, You cant create a symlink in a windows mount
# Copy over the content first and then link backwards.
if [ ! -L /home/tomcat7/ceh-catalogue/webapps ] 
  then
    sudo service tomcat7-ceh-catalogue stop
    sudo mv /home/tomcat7/ceh-catalogue/webapps /vagrant/catalogue_tomcat

    # Create links backwards
    sudo ln -s /vagrant/catalogue_tomcat /home/tomcat7/ceh-catalogue/webapps
    sudo service tomcat7-ceh-catalogue start
fi