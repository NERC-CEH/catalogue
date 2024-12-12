#!/usr/bin/env bash

set -u

die () { exit 1; }

usage () {
    cat <<EOF
Usage: ${0##*/} [-hwb]
Rebuild and start a catalogue instance.

	-h	Show this help and exit
	-w	Don't build the web assets
	-b	Start Hubbub docker service and enable its profile

This script sets Spring environment variables before starting the
catalogue, but will not override them if they are already set.  It may
not work properly if some of them are set to unexpected values: use
them at your own risk.
EOF
}

build_web=true
with_hubbub=false
while getopts hwb opt; do
    case $opt in
        h)
            usage
            exit
            ;;
        w)
            build_web=false
            ;;
        b)
            with_hubbub=true
            ;;
        *)
            usage >&2
            exit 2
            ;;
    esac
done

TOP=$(git rev-parse --show-toplevel) || die
cd "$TOP" || die

if [[ $build_web = true ]]; then
    echo 'Building web assets...'
    (
        cd web || die
        npm run build-dev || die
        npm run build-css || die
    ) || die
    echo 'Built web assets.'
fi

echo 'Setting up static directory...'
mkdir -p static
[[ -d static/img ]] || ln -sr web/img static/ || die
[[ -d static/css ]] || ln -sr web/css static/ || die
[[ -d static/scripts ]] || ln -sr web/dist static/scripts || die
[[ -d static/css/images ]] || ln -sr web/node_modules/leaflet-draw/dist/images static/css/ || die
[[ -d static/webfonts ]] || ln -sr web/node_modules/@fortawesome/fontawesome-free/webfonts static/ || die
echo 'Static directory configured.'

echo 'Setting up datastore git repo...'
(
    mkdir -p datastore
    cd datastore || die
    if [[ -d .git ]]; then
        echo 'Datastore git repo already exists, continuing.'
        exit
    fi
    cp ../fixtures/datastore/REV-1/* . || die
    git init --initial-branch=main || die
    git add --all || die
    git commit --author='Dummy User <info@eidc.ac.uk>' --allow-empty-message --message='' || die
    echo 'Datastore configured.'
) || die

echo 'Starting dependent services...'
if [[ $with_hubbub = true ]]; then
    docker compose --profile hubbub up --wait --detach || die
else
    docker compose up --wait --detach || die
fi

readarray -t secrets < <(grep -E -v '^$|^#' secrets.env 2>/dev/null)
if (( ${#secrets[@]} > 0 )); then
    export "${secrets[@]}"
fi

export_default () {
    [[ -v $1 ]] || export "$1"="$2"
}

export_default DATA_REPOSITORY_LOCATION "$TOP"/datastore
export_default DOCUMENTS_BASEURI http://localhost:8080
export_default DOI_API https://api.test.datacite.org/dois
export_default GEMET_LOCAL "$PWD"/fixtures/vocabs/gemet.json
export_default HUBBUB_LOCATION "$PWD"/dropbox
export_default HUBBUB_URL http://localhost:8082/v7
export_default JENA_LOCATION "$TOP"/fixtures/jena
export_default JIRA_SERVICEAGREEMENT_PREFIX CT-
export_default MANAGEMENT_SERVER_PORT 8091
export_default MAPS_LOCATION "$TOP"/mapfiles
export_default METRICS_DATABASE_URL jdbc:sqlite:"$TOP"/metrics-db/metrics.db
export_default SCHEMAS_LOCATION "$TOP"/schemas
export_default SERVER_PORT 8090
export_default SOLR_SERVER_URL http://localhost:8983/solr
export_default SPRING_FREEMARKER_CACHE false
export_default SPRING_FREEMARKER_TEMPLATE_LOADER_PATH file:"$TOP"/templates
export_default SPRING_SERVLET_MULTIPART_LOCATION "$TOP"/dropbox
export_default SPRING_WEB_RESOURCES_STATIC_LOCATIONS file:"$TOP"/static
export_default UPLOAD_SIMPLE_DATASTORE "$TOP"/datastore

if [[ $with_hubbub = true ]]; then
    export_default SPRING_PROFILES_ACTIVE development,upload:hubbub,server:eidc,search:basic,service-agreement
else
    export_default SPRING_PROFILES_ACTIVE development,upload:simple,server:eidc,search:basic,service-agreement
fi

echo 'Building and starting Java application...'

exec ./gradlew bootRun
