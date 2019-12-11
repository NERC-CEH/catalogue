FROM solr:8.3
LABEL maintainer="oss@ceh.ac.uk"

COPY documents /opt/solr/server/solr/configsets/documents

CMD ["solr-precreate", "documents", "/opt/solr/server/solr/configsets/documents"]
