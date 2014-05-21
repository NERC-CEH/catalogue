# Stupid windows, You cant create a symlink in a windows mount
# Copy over the content first and then link backwards.
if [ ! -L /home/tomcat7/project/webapps ] 
  then
    sudo service tomcat7-project stop
    sudo mv /home/tomcat7/project/webapps /vagrant/ukeof_tomcat

    # Create links backwards
    sudo ln -s /vagrant/ukeof_tomcat /home/tomcat7/project/webapps
    sudo service tomcat7-project start
fi
