FROM solr:10.0.0
LABEL maintainer="oss@ceh.ac.uk"

COPY jts-core-1.20.0.jar /opt/solr/server/solr-webapp/webapp/WEB-INF/lib/
COPY create-cores /opt/solr/docker/scripts

# Stock Solr logging config plus one targeted mute; see the header of the file itself.
#
# Re-base it on a Solr upgrade by diffing against the new image's stock copy:
#   docker run --rm --entrypoint cat solr:<version> \
#     /opt/solr/server/resources/log4j2.xml | diff - solr/log4j2.xml
# Anything beyond the io.opentelemetry line is an upstream change to carry across.
COPY log4j2.xml /opt/solr/server/resources/log4j2.xml

# Load that file directly instead of the copy under /var/solr.
#
# The base image sets LOG4J_PROPS=/var/solr/log4j2.xml, and /var/solr is a declared VOLUME.
# The entrypoint's init-var-solr seeds that path from /opt/solr/server/resources/log4j2.xml
# ONLY when the file is absent, so on any deployment with a persistent /var/solr - i.e. the
# prod PVC, which was seeded on its first ever start - a new image's logging config is
# silently ignored and the stale copy on the volume keeps winning.
#
# Pointing LOG4J_PROPS at the in-image path makes the config travel with the image, which is
# the whole point of keeping it here. Trade-off: logging can no longer be changed by editing
# the file on the volume, only by rebuilding (or at runtime via /solr/admin/info/logging).
ENV LOG4J_PROPS=/opt/solr/server/resources/log4j2.xml

COPY documents /opt/solr/server/solr/configsets/documents
COPY keywords /opt/solr/server/solr/configsets/keywords
COPY organisations /opt/solr/server/solr/configsets/organisations

CMD ["create-cores"]
