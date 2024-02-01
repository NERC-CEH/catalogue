# Build webpack (javascript & css)
FROM node:21.5.0-alpine3.19 AS build-web
WORKDIR /web
COPY web/Gruntfile.js web/package.json web/package-lock.json web/webpack.js ./
RUN --mount=type=cache,target=/web/.npm npm ci --no-audit
COPY web/img ./img
COPY web/less ./less
COPY web/src ./src
RUN npm run build-css
RUN npm run build-prod

# Build Java
FROM gradle:8.5-jdk17-alpine AS build-java
WORKDIR /app
COPY --chown=gradle:gradle java/build.gradle .
COPY --chown=gradle:gradle java/lombok.config .
COPY --chown=gradle:gradle java/src src/
RUN gradle bootJar
WORKDIR build/libs
RUN java -Djarmode=layertools -jar app.jar extract

# Create production image
FROM eclipse-temurin:17-alpine AS prod
LABEL maintainer="oss@ceh.ac.uk"
RUN apk --no-cache add curl
RUN addgroup -g 1001 -S spring && adduser -u 1001 -S spring -G spring
RUN mkdir -p /var/ceh-catalogue/datastore /var/ceh-catalogue/dropbox /var/ceh-catalogue/mapfiles /var/ceh-catalogue/tdb /var/upload/datastore
WORKDIR /app
COPY --chown=spring:spring schemas /opt/ceh-catalogue/schemas
COPY --chown=spring:spring --from=build-java /app/build/libs/dependencies/ ./
COPY --chown=spring:spring --from=build-java /app/build/libs/spring-boot-loader/ ./
COPY --chown=spring:spring --from=build-java /app/build/libs/snapshot-dependencies/ ./
COPY --chown=spring:spring --from=build-java /app/build/libs/application/ ./
COPY --chown=spring:spring templates /opt/ceh-catalogue/templates
COPY --chown=spring:spring --from=build-web /web/img /opt/ceh-catalogue/static/img
COPY --chown=spring:spring --from=build-web /web/dist /opt/ceh-catalogue/static/scripts
COPY --chown=spring:spring --from=build-web /web/css /opt/ceh-catalogue/static/css
COPY --chown=spring:spring --from=build-web /web/node_modules/leaflet-draw/dist/images /opt/ceh-catalogue/static/css/images
COPY --chown=spring:spring --from=build-web /web/node_modules/@fortawesome/fontawesome-free/webfonts /opt/ceh-catalogue/static/fonts
RUN chown spring:spring -R /var/ceh-catalogue && chown spring:spring -R /var/upload
VOLUME ["/var/ceh-catalogue/datastore", "/var/ceh-catalogue/dropbox", "/var/ceh-catalogue/mapfiles", "/var/upload/datastore"]
EXPOSE 8080 8081
USER spring
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
HEALTHCHECK --start-period=30s CMD curl --no-progress-meter --output - --fail http://localhost:8081/actuator/health || exit 1

# Create Datalabs image
FROM prod as datalabs
USER root

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

