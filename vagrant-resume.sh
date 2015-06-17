#!/bin/sh
vagrant up
vagrant ssh -c 'sudo service tomcat7-solr restart; \
                sudo service tomcat7-ceh-catalogue restart'