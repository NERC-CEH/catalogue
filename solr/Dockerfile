FROM solr:9.10.1
LABEL maintainer="oss@ceh.ac.uk"

COPY jts-core-1.20.0.jar /opt/solr/server/solr-webapp/webapp/WEB-INF/lib/
COPY create-cores /opt/solr/docker/scripts

COPY documents /opt/solr/server/solr/configsets/documents
COPY keywords /opt/solr/server/solr/configsets/keywords
COPY organisations /opt/solr/server/solr/configsets/organisations

CMD ["create-cores"]
