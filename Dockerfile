FROM tomcat:8.0-jre8
MAINTAINER oss@ceh.ac.uk

RUN echo 'CATALINA_OPTS="                                                            \
 -Dspring.profiles.active=${SPRING_PROFILES}                                         \
 -Ddocuments.baseUri=${BASE_URI}                                                     \
 -Dmaps.location=/var/ceh-catalogue/mapfiles                                         \
 -Djena.location=/var/ceh-catalogue/tdb                                              \
 -Ddata.repository.location=/var/ceh-catalogue/datastore                             \
 -Dschemas.location=/opt/ceh-catalogue/schemas                                       \
 -Dtemplate.location=/opt/ceh-catalogue/templates                                    \
 -Dsolr.server.documents.url=http://solr:8080/documents                              \
 -Duserstore.crowd.address=https://crowd.ceh.ac.uk/crowd/rest/usermanagement/latest  \
 -Duserstore.crowd.username=${CROWD_USERNAME}                                        \
 -Duserstore.crowd.password=${CROWD_PASSWORD}                                        \
 -Ddoi.prefix=10.5285/                                                               \
 -Ddoi.username=BL.NERC                                                              \
 -Ddoi.password=${DOI_PASSWORD}                                                      \
"' >> /usr/local/tomcat/bin/setenv.sh

RUN rm -Rf /usr/local/tomcat/webapps/*
COPY schemas              /opt/ceh-catalogue/schemas
COPY templates            /opt/ceh-catalogue/templates
COPY web/src              /opt/ceh-catalogue/web
COPY dropbox              /var/ceh-catalogue/dropbox
COPY java/target/ROOT.war /tmp/ROOT.war
RUN unzip -d /usr/local/tomcat/webapps/ROOT /tmp/ROOT.war

RUN ln -s /opt/ceh-catalogue/web /usr/local/tomcat/webapps/ROOT/static
