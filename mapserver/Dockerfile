FROM ubuntu:16.04
MAINTAINER oss@ceh.ac.uk

RUN apt-get update; apt-get install -y \
  cgi-mapserver \
  mapserver-bin \
  gdal-bin \
  apache2 \
  libapache2-mod-fcgid

COPY helpers /mapserver/helpers
COPY mapserver.conf /etc/apache2/sites-available/10-mapserver.conf

RUN a2enmod fcgid rewrite; \
    a2dissite 000-default; \
    a2ensite 10-mapserver

CMD apache2ctl -D FOREGROUND