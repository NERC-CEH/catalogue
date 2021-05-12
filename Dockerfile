# Build web (javascript & css)
FROM node:15.11.0-stretch AS build-web
WORKDIR /app
COPY --chown=1000:1000 web/src/less src/less
COPY --chown=1000:1000 web/src/scripts src/scripts
COPY --chown=1000:1000 web/.bowerrc .
COPY --chown=1000:1000 web/bower.json .
COPY --chown=1000:1000 web/Gruntfile.js .
COPY --chown=1000:1000 web/package.json .
COPY --chown=1000:1000 web/package-lock.json .
RUN npm install
RUN node_modules/.bin/bower install --allow-root
RUN node_modules/.bin/grunt

# Build Java
FROM gradle:6.8.3-jdk15 AS build-java
WORKDIR /app
COPY --chown=gradle:gradle java/src src/
COPY --chown=gradle:gradle java/build.gradle .
COPY --chown=gradle:gradle java/lombok.config .
COPY --chown=gradle:gradle --from=build-web /app/src/css src/main/resources/static/css
COPY --chown=gradle:gradle web/src/img src/main/resources/static/img
COPY --chown=gradle:gradle --from=build-web /app/src/scripts/main-out.js src/main/resources/static/scripts/main-out.js
COPY --chown=gradle:gradle --from=build-web /app/src/vendor/font-awesome-5/webfonts src/main/resources/static/vendor/font-awesome-5/webfonts
COPY --chown=gradle:gradle --from=build-web /app/src/vendor/requirejs/require.js src/main/resources/static/vendor/requirejs/require.js
COPY templates src/main/resources/templates
RUN chown -R gradle:gradle src/main/resources
RUN gradle bootJar
WORKDIR build/libs
RUN java -Djarmode=layertools -jar app.jar extract
RUN ls -lAhR application

# Create production image
FROM openjdk:15-alpine AS prod
LABEL maintainer="oss@ceh.ac.uk"
WORKDIR /app
COPY schemas /opt/ceh-catalogue/schemas
COPY --from=build-java /app/build/libs/dependencies/ ./
COPY --from=build-java /app/build/libs/spring-boot-loader/ ./
COPY --from=build-java /app/build/libs/snapshot-dependencies/ ./
COPY --from=build-java /app/build/libs/application/ ./
VOLUME ["/var/ceh-catalogue", "/var/upload/datastore"]
EXPOSE 8080
EXPOSE 8081
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
HEALTHCHECK CMD curl --fail http://localhost:8081/actuator/health || exit 1

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
COPY --from=datastore /datastore /var/ceh-catalogue/datastore

