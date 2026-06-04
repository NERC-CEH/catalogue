#!/bin/sh
set -e

mkdir -p /app/static /app/mapfiles /app/metrics-db /app/dropbox /app/web/css

[ -L /app/static/img ]        || ln -sf /app/web/img /app/static/img
[ -L /app/static/css ]        || ln -sf /app/web/css /app/static/css
[ -L /app/static/scripts ]    || ln -sf /app/web/dist /app/static/scripts
[ -L /app/static/css/images ] || ln -sf /app/web/node_modules/leaflet-draw/dist/images /app/static/css/images
[ -L /app/static/webfonts ]   || ln -sf /app/web/node_modules/@fortawesome/fontawesome-free/webfonts /app/static/webfonts

exec ./gradlew :java:bootRun "$@"
