FROM solr:8.3
LABEL maintainer="oss@ceh.ac.uk"

COPY jts-core-1.16.1.jar /opt/solr/server/solr-webapp/webapp/WEB-INF/lib/
COPY documents /opt/solr/server/solr/configsets/documents

CMD ["solr-precreate", "documents", "/opt/solr/server/solr/configsets/documents"]
