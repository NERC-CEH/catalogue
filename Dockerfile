# syntax=docker/dockerfile:1
# Build webpack (javascript & css)
FROM node:24-alpine AS build-web
WORKDIR /web
RUN apk --no-cache upgrade
COPY web/package.json web/package-lock.json web/webpack.js web/gulpfile.js ./
RUN --mount=type=cache,target=/web/.npm npm ci --cache /web/.npm --no-audit
COPY web/img ./img
COPY web/scss ./scss
COPY web/src ./src
RUN npm run build-css
RUN npm run build-prod

# Build Java
FROM gradle:9.5.1-jdk25-alpine AS build-java
WORKDIR /app
RUN apk --no-cache upgrade
COPY --chown=gradle:gradle java/lombok.config .
COPY --chown=gradle:gradle java/build.gradle .
COPY --chown=gradle:gradle gradle/libs.versions.toml gradle/
COPY --chown=gradle:gradle java/src src/
# --secret keeps CI_JOB_TOKEN (unique per CI run) out of image layers and the cache key,
# unlike --build-arg, which would bake it into `docker history` and bust the gradle cache mount
RUN --mount=type=cache,target=/root/.gradle \
    --mount=type=secret,id=gitlab_token \
    CI_JOB_TOKEN="$(cat /run/secrets/gitlab_token 2>/dev/null || true)" gradle bootJar --no-daemon
WORKDIR build/libs
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher

# Create production image
FROM eclipse-temurin:25.0.3_9-jdk-alpine-3.23 AS prod
LABEL maintainer="oss@ceh.ac.uk"
ARG BUILD_DATE=unknown
RUN apk --no-cache upgrade
RUN addgroup -g 1001 -S spring && adduser -u 1001 -S spring -G spring
RUN mkdir -p  \
    /var/ceh-catalogue/datastore \
    /var/ceh-catalogue/dropbox \
    /var/ceh-catalogue/mapfiles \
    /var/ceh-catalogue/metrics-db \
    /var/ceh-catalogue/ror \
    /var/ceh-catalogue/tdb \
    /var/upload/datastore
WORKDIR /app
COPY --chown=spring:spring --from=build-java /app/build/libs/app/dependencies/ ./
COPY --chown=spring:spring --from=build-java /app/build/libs/app/spring-boot-loader/ ./
COPY --chown=spring:spring --from=build-java /app/build/libs/app/snapshot-dependencies/ ./
COPY --chown=spring:spring --from=build-java /app/build/libs/app/application/ ./
COPY --chown=spring:spring templates /opt/ceh-catalogue/templates
COPY --chown=spring:spring --from=build-web /web/img /opt/ceh-catalogue/static/img
COPY --chown=spring:spring --from=build-web /web/dist /opt/ceh-catalogue/static/scripts
COPY --chown=spring:spring --from=build-web /web/css /opt/ceh-catalogue/static/css
COPY --chown=spring:spring --from=build-web /web/node_modules/leaflet-draw/dist/images /opt/ceh-catalogue/static/css/images
COPY --chown=spring:spring --from=build-web /web/node_modules/@fortawesome/fontawesome-free/webfonts /opt/ceh-catalogue/static/webfonts
RUN chown spring:spring -R /var/ceh-catalogue && chown spring:spring -R /var/upload
VOLUME /var/ceh-catalogue/datastore \
       /var/ceh-catalogue/dropbox \
       /var/ceh-catalogue/mapfiles \
       /var/ceh-catalogue/metrics-db \
       /var/ceh-catalogue/ror \
       /var/upload/datastore
EXPOSE 8080 8081
USER spring
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

# Create resources for development only
FROM alpine/git:v2.30.1 AS datastore
COPY fixtures/datastore/REV-1 /datastore
WORKDIR /datastore
RUN git config --global init.defaultBranch main \
    && git init \
    && git config user.email "test@example.com" \
    && git config user.name "test" \
    && git add -A \
    && git commit -m "data loading"

# Development image
FROM prod AS dev
COPY --chown=spring:spring --from=datastore /datastore /var/ceh-catalogue/datastore
RUN apk --no-cache add git vim

# Development run image — mounts full project tree as a volume at /app
FROM gradle:9.5.1-jdk25-alpine AS dev-run
USER root
RUN apk --no-cache upgrade && apk --no-cache add su-exec
COPY --from=datastore /datastore /var/ceh-catalogue/datastore
RUN chown -R gradle:gradle /var/ceh-catalogue/datastore
COPY docker/entrypoint-dev.sh /usr/local/bin/entrypoint-dev.sh
RUN chmod +x /usr/local/bin/entrypoint-dev.sh
WORKDIR /app
# No `USER gradle`: the entrypoint starts as root, repairs datastore ownership
# on a persisted named volume, then drops to gradle via su-exec.
ENTRYPOINT ["/usr/local/bin/entrypoint-dev.sh"]
