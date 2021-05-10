FROM solr:8.3
LABEL maintainer="oss@ceh.ac.uk"

COPY jts-core-1.16.1.jar /opt/solr/server/solr-webapp/webapp/WEB-INF/lib/
COPY documents /opt/solr/server/solr/configsets/documents
CMD ["sh", "-c","/opt/docker-solr/scripts/precreate-core documents /opt/solr/server/solr/configsets/documents; /opt/docker-solr/scripts/precreate-core uk.ac.ceh.gateway.catalogue.deims /opt/solr/server/solr/configsets/uk.ac.ceh.gateway.catalogue.deims; exec solr -f"]