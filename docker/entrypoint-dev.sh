#!/bin/sh
set -e

# When started as root (dev-run image), repair datastore ownership that a
# persisted named volume may have seeded as root, then drop to the gradle user.
# Named volumes keep their first-seeded ownership across rebuilds, so the
# build-time chown alone cannot fix a volume created by an older image.
if [ "$(id -u)" = 0 ]; then
  if [ "$(stat -c '%u' /var/ceh-catalogue/datastore)" != "1000" ]; then
    chown -R gradle:gradle /var/ceh-catalogue/datastore
  fi
  # Repair any root-owned Gradle build output left in the bind-mounted project by an
  # earlier root-context invocation (e.g. `docker compose run --entrypoint`), so that
  # host-side Gradle can overwrite it. `find` only touches mis-owned entries, so this is
  # cheap on an already-correct tree.
  for d in /app/build /app/java/build; do
    if [ -d "$d" ]; then
      find "$d" ! -user gradle -exec chown gradle:gradle {} + 2>/dev/null || true
    fi
  done
  exec su-exec gradle "$0" "$@"
fi

mkdir -p /app/static /app/mapfiles /app/metrics-db /app/dropbox /app/web/css

[ -L /app/static/img ]        || ln -sf /app/web/img /app/static/img
[ -L /app/static/css ]        || ln -sf /app/web/css /app/static/css
[ -L /app/static/scripts ]    || ln -sf /app/web/dist /app/static/scripts
[ -L /app/static/css/images ] || ln -sf /app/web/node_modules/leaflet-draw/dist/images /app/static/css/images
[ -L /app/static/webfonts ]   || ln -sf /app/web/node_modules/@fortawesome/fontawesome-free/webfonts /app/static/webfonts

exec ./gradlew :java:bootRun "$@"
