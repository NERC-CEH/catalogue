FROM solr:8.9
LABEL maintainer="oss@ceh.ac.uk"

COPY jts-core-1.18.1.jar /opt/solr/server/solr-webapp/webapp/WEB-INF/lib/
COPY create-cores /opt/docker-solr/scripts

COPY deims /opt/solr/server/solr/configsets/deims
COPY documents /opt/solr/server/solr/configsets/documents
COPY service-agreements /opt/solr/server/solr/configsets/service-agreements

CMD ["create-cores"]
