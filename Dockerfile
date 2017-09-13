FROM tomcat:8.5-jre8
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
 -Djira.address=${JIRA_ADDRESS}                                                      \
 -Dplone.address=${PLONE_ADDRESS}                                                      \
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
COPY java/build/libs/ROOT.war /opt/ceh-catalogue/libs/ROOT.war
RUN unzip -od /usr/local/tomcat/webapps/ROOT /opt/ceh-catalogue/libs/ROOT.war

HEALTHCHECK CMD curl --fail http://localhost:8080/eidc/documents || exit 1

RUN ln -s /opt/ceh-catalogue/web /usr/local/tomcat/webapps/ROOT/static
