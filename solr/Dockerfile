FROM tomcat:8.0-jre8
MAINTAINER oss@ceh.ac.uk

ENV CATALINA_OPTS "-Dsolr.solr.home=/var/solr"

RUN rm -Rf /usr/local/tomcat/webapps/*
ADD https://repo1.maven.org/maven2/org/apache/solr/solr/4.6.0/solr-4.6.0.war /tmp/solr.war
RUN unzip -d /usr/local/tomcat/webapps/ROOT /tmp/solr.war

ADD https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.6.6/slf4j-api-1.6.6.jar /usr/local/tomcat/lib/slf4j-api.jar
ADD https://repo1.maven.org/maven2/org/slf4j/slf4j-log4j12/1.6.6/slf4j-log4j12-1.6.6.jar /usr/local/tomcat/lib/slf4j-log4j12.jar
ADD https://repo1.maven.org/maven2/org/slf4j/jcl-over-slf4j/1.6.6/jcl-over-slf4j-1.6.6.jar /usr/local/tomcat/lib/jcl-over-slf4j.jar
ADD https://repo1.maven.org/maven2/org/slf4j/jul-to-slf4j/1.6.6/jul-to-slf4j-1.6.6.jar /usr/local/tomcat/lib/jul-to-slf4j.jar
ADD https://repo1.maven.org/maven2/log4j/log4j/1.2.16/log4j-1.2.16.jar /usr/local/tomcat/lib/log4j.jar
ADD https://repo1.maven.org/maven2/com/vividsolutions/jts/1.13/jts-1.13.jar /usr/local/tomcat/lib/jts.jar

COPY config /var/solr

HEALTHCHECK CMD curl --fail http://localhost:8080/documents/select?q=*%3A* || exit 1
