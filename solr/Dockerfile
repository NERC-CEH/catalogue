FROM solr:9.4.0
LABEL maintainer="oss@ceh.ac.uk"

COPY jts-core-1.19.0.jar /opt/solr/server/solr-webapp/webapp/WEB-INF/lib/
COPY create-cores /opt/solr/docker/scripts

COPY deims /opt/solr/server/solr/configsets/deims
COPY documents /opt/solr/server/solr/configsets/documents
COPY keywords /opt/solr/server/solr/configsets/keywords

CMD ["create-cores"]
