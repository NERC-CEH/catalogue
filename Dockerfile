FROM tomcat:8.5.31-jre8
LABEL maintainer="oss@ceh.ac.uk"

RUN rm -Rf /usr/local/tomcat/webapps/*
COPY schemas              /opt/ceh-catalogue/schemas
COPY templates            /opt/ceh-catalogue/templates
COPY web/src              /opt/ceh-catalogue/web
COPY java/build/libs/ROOT.war /opt/ceh-catalogue/libs/ROOT.war
RUN unzip -od /usr/local/tomcat/webapps/ROOT /opt/ceh-catalogue/libs/ROOT.war

EXPOSE 8080

HEALTHCHECK CMD curl --fail http://localhost:8080/eidc/documents || exit 1

RUN ln -s /opt/ceh-catalogue/web /usr/local/tomcat/webapps/ROOT/static
