#!/bin/sh
set -e

mkdir -p /app/static /app/mapfiles /app/metrics-db /app/dropbox

[ -e /app/static/img ]        || ln -sr /app/web/img /app/static/
[ -e /app/static/css ]        || ln -sr /app/web/css /app/static/
[ -e /app/static/scripts ]    || ln -sr /app/web/dist /app/static/scripts
[ -e /app/static/css/images ] || ln -sr \
    /app/web/node_modules/leaflet-draw/dist/images /app/static/css/images
[ -e /app/static/webfonts ]   || ln -sr \
    /app/web/node_modules/@fortawesome/fontawesome-free/webfonts /app/static/webfonts

exec ./gradlew :java:bootRun "$@"
