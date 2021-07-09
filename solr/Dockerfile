FROM solr:8.3
LABEL maintainer="oss@ceh.ac.uk"

COPY jts-core-1.16.1.jar /opt/solr/server/solr-webapp/webapp/WEB-INF/lib/
COPY documents /opt/solr/server/solr/configsets/documents
COPY deims /opt/solr/server/solr/configsets/deims
CMD ["sh", "-c", "precreate-core documents /opt/solr/server/solr/configsets/documents; precreate-core deims /opt/solr/server/solr/configsets/deims; precreate-core keywords /opt/solr/server/solr/configsets/keywords; solr-foreground"]
