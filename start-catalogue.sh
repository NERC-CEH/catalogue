#!/usr/bin/env bash

set -u

die () { exit 1; }

usage () {
    cat <<EOF
Usage: ${0##*/} [-hwblf] [[--] args ...]
Rebuild and start a catalogue instance.

    -h  Show this help and exit
    -w  Don't build the web assets
    -b  Start Hubbub docker service and enable its profile
    -l  Start Legilo service and enable its profile
    -f  Start Fuseki service

Any remaining arguments are passed on to ./gradlew bootRun.  Use
-- to separate them if they start with something that looks like
another option.

This script sets Spring environment variables before starting the
catalogue, but will not override them if they are already set.  It may
not work properly if some of them are set to unexpected values: use
them at your own risk.
EOF
}

build_web=true
with_hubbub=false
with_legilo=false
with_fuseki=false
while getopts hwblf opt; do
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
        l)
            with_legilo=true
            ;;
        f)
            with_fuseki=true
            ;;
        *)
            usage >&2
            exit 2
            ;;
    esac
done
shift "$((OPTIND - 1))"

TOP=$(git rev-parse --show-toplevel) || die
cd "$TOP" || die

if [[ $build_web = true ]]; then
    echo 'Building web assets...'
    (cd web && npm install && npm run build-dev && npm run build-css-dev) || die
    echo 'Built web assets.'
fi

echo 'Setting up static directory...'
# shellcheck disable=SC2015 # && and || interaction is intended in this case
mkdir -p static &&
{ [[ -d static/img ]] || ln -sr web/img static/; } &&
{ [[ -d static/css ]] || ln -sr web/css static/; } &&
{ [[ -d static/scripts ]] || ln -sr web/dist static/scripts; } &&
{ [[ -d static/css/images ]] || ln -sr web/node_modules/leaflet-draw/dist/images static/css/; } &&
{ [[ -d static/webfonts ]] || ln -sr web/node_modules/@fortawesome/fontawesome-free/webfonts static/; } || die
echo 'Static directory configured.'

echo 'Setting up mapfile directory'
mkdir -p mapfiles

echo 'Setting up datastore git repo...'
# shellcheck disable=SC2015
mkdir -p datastore &&
(
    cd datastore &&
    if [[ -d .git ]]; then
        echo 'Datastore git repo already exists, continuing.'
        exit
    fi &&
    cp -r ../fixtures/datastore/REV-1/* . &&
    git init --initial-branch=main &&
    git add --all &&
    git commit --author='Dummy User <info@eidc.ac.uk>' --allow-empty-message --message='' &&
    echo 'Datastore configured.'
) || die

echo 'Starting dependent services...'
profile=''
if [[ $with_hubbub = true ]]; then
    profile="$profile --profile hubbub"
fi
if [[ $with_legilo = true ]]; then
    profile="$profile --profile legilo"
fi
if [[ $with_fuseki = true ]]; then
    profile="$profile --profile fuseki"
fi
docker compose $profile up --wait --detach
if [[ $with_hubbub = true ]]; then
    db_init=`docker exec -it catalogue-hubbub-db-1 sh -c "test -f /hubbub_backup.sql && echo -n 'yes'"`
    if [[ $db_init != 'yes' ]]; then
        docker cp ./fixtures/hubbub/init/hubbub_backup.sql catalogue-hubbub-db-1:/
        docker exec -it catalogue-hubbub-db-1 psql -U gardener -d hubbub -f /hubbub_backup.sql 1>/dev/null
    fi
fi

export_default () {
    [[ -v $1 ]] || export "$1"="$2"
}

export_default DATA_REPOSITORY_LOCATION "$TOP"/datastore
export_default DOCUMENTS_BASEURI http://localhost:8080
export_default DOI_API https://api.test.datacite.org/dois
export_default GEMET_LOCAL "$TOP"/fixtures/vocabs/gemet.json
export_default ROR_LOCAL "$TOP"/fixtures/solr/ror
export_default HUBBUB_LOCATION "$TOP"/dropbox
export_default HUBBUB_URL http://localhost:8082/v7
export_default JENA_LOCATION "$TOP"/fixtures/jena
export_default JIRA_SERVICEAGREEMENT_PREFIX CT-
export_default JIRA_DEPOSITREQUEST_PROJECT CT
export_default MANAGEMENT_SERVER_PORT 8091
export_default MAPS_LOCATION "$TOP"/mapfiles
export_default METRICS_DATABASE_URL jdbc:sqlite:"$TOP"/metrics-db/metrics.db
export_default SERVER_PORT 8090
export_default SOLR_SERVER_URL http://localhost:8983/solr
export_default SPRING_FREEMARKER_CACHE false
export_default SPRING_FREEMARKER_TEMPLATE_LOADER_PATH file:"$TOP"/templates
export_default SPRING_SERVLET_MULTIPART_LOCATION "$TOP"/dropbox
export_default SPRING_WEB_RESOURCES_STATIC_LOCATIONS file:"$TOP"/static
export_default UPLOAD_SIMPLE_DATASTORE "$TOP"/datastore
export_default SPRING_DEVTOOLS_RESTART_ENABLED true
export_default SPRING_DEVTOOLS_LIVERELOAD_ENABLED true
export_default LEGILO_URL http://192.168.211.128:8000
export_default LEGILO_USER user
export_default LEGILO_PASSWORD password

spring_profile='development,server:eidc,search:basic,cache,service-agreement,keyword-suggestions'
if [[ $with_hubbub = true ]]; then
    spring_profile="$spring_profile,upload:hubbub"
else
    spring_profile="$spring_profile,upload:simple"
fi
if [[ $with_legilo = true ]]; then
    spring_profile="$spring_profile,keyword-suggestions"
fi
export_default SPRING_PROFILES_ACTIVE $spring_profile

readarray -t external_env < <(grep -E -vh '^$|^#' secrets.env override.env 2>/dev/null)
if (( ${#external_env[@]} > 0 )); then
    export "${external_env[@]}"
fi

echo 'Building and starting Java application...'

exec ./gradlew bootRun "$@"
