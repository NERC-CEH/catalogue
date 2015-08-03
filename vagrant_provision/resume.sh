#!/bin/sh
# Remove any indexes stored on the box
rm -Rf /var/ceh-catalogue/tdb
rm -Rf /opt/ceh-catalogue/catalogue/solr-config/documents/data/

# Restart the services
service tomcat7-solr restart
service tomcat7-ceh-catalogue restart
