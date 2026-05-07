# Build webpack (javascript & css)
FROM node:22-alpine AS build-web
WORKDIR /web
COPY web/package.json web/package-lock.json web/webpack.js web/gulpfile.js ./
RUN --mount=type=cache,target=/web/.npm npm ci --cache /web/.npm --no-audit
COPY web/img ./img
COPY web/scss ./scss
COPY web/src ./src
RUN npm run build-css
RUN npm run build-prod

# Build Java
FROM gradle:9.4.1-jdk25-alpine AS build-java
WORKDIR /app
COPY --chown=gradle:gradle java/lombok.config .
COPY --chown=gradle:gradle java/build.gradle .
COPY --chown=gradle:gradle gradle/libs.versions.toml gradle/
COPY --chown=gradle:gradle java/src src/
RUN --mount=type=cache,target=/root/.gradle gradle bootJar --no-daemon
WORKDIR build/libs
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher

# Create production image
FROM eclipse-temurin:25-alpine AS prod
LABEL maintainer="oss@ceh.ac.uk"
RUN apk --no-cache upgrade && apk --no-cache add curl
RUN addgroup -g 1001 -S spring && adduser -u 1001 -S spring -G spring
RUN mkdir -p /var/ceh-catalogue/datastore /var/ceh-catalogue/dropbox /var/ceh-catalogue/mapfiles /var/ceh-catalogue/tdb /var/upload/datastore /var/ceh-catalogue/metrics-db /var/ceh-catalogue/ror
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
VOLUME ["/var/ceh-catalogue/datastore", "/var/ceh-catalogue/dropbox", "/var/ceh-catalogue/mapfiles", "/var/upload/datastore", "/var/ceh-catalogue/metrics-db", "/var/ceh-catalogue/ror"]
EXPOSE 8080 8081
USER root
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
HEALTHCHECK --start-period=30s CMD curl --no-progress-meter --output - --fail http://localhost:8081/actuator/health || exit 1

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
USER root
RUN apk --no-cache add git vim
USER spring

