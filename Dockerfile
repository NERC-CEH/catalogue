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


# Build Permission app (javascript & css)
FROM node:17.4.0 AS build-permissions
WORKDIR web/permission
COPY --chown=1000:1000 web/permission/package.json .
COPY --chown=1000:1000 web/permission/package-lock.json .
COPY --chown=1000:1000 web/permission/webpack.config.js .
COPY --chown=1000:1000 web/permission/src src/
RUN npm install && npm run build

# Build Catalogue app (javascript & css)
FROM node:17.4.0 AS build-catalogue
WORKDIR web/catalogue
COPY --chown=1000:1000 web/catalogue/package.json .
COPY --chown=1000:1000 web/catalogue/package-lock.json .
COPY --chown=1000:1000 web/catalogue/webpack.config.js .
COPY --chown=1000:1000 web/catalogue/src src/
RUN npm install && npm run build

# Build simple upload app (javascript & css)
FROM node:17.4.0 AS build-simple-upload
WORKDIR web/simple-upload
COPY --chown=1000:1000 web/simple-upload/package.json .
COPY --chown=1000:1000 web/simple-upload/package-lock.json .
COPY --chown=1000:1000 web/simple-upload/webpack.config.js .
COPY --chown=1000:1000 web/simple-upload/src src/
RUN npm install && npm run build

# Build hubbub app (javascript & css)
FROM node:17.4.0 AS build-hubbub
WORKDIR web/hubbub
COPY --chown=1000:1000 web/hubbub/package.json .
COPY --chown=1000:1000 web/hubbub/package-lock.json .
COPY --chown=1000:1000 web/hubbub/webpack.config.js .
COPY --chown=1000:1000 web/hubbub/src src/
RUN npm install && npm run build

# Build Java
FROM gradle:7.2-jdk16 AS build-java
WORKDIR /app
COPY --chown=gradle:gradle java/src src/
COPY --chown=gradle:gradle java/build.gradle .
COPY --chown=gradle:gradle java/lombok.config .
RUN gradle bootJar
WORKDIR build/libs
RUN java -Djarmode=layertools -jar app.jar extract

# Create production image
FROM openjdk:16-alpine AS prod
LABEL maintainer="oss@ceh.ac.uk"
RUN apk --no-cache add curl
RUN addgroup -g 1001 -S spring && adduser -u 1001 -S spring -G spring
RUN mkdir -p /var/ceh-catalogue/datastore /var/ceh-catalogue/dropbox /var/ceh-catalogue/mapfiles /var/ceh-catalogue/tdb /var/upload/datastore
WORKDIR /app
COPY schemas /opt/ceh-catalogue/schemas
COPY  --from=build-java /app/build/libs/dependencies/ ./
COPY --from=build-java /app/build/libs/spring-boot-loader/ ./
COPY --from=build-java /app/build/libs/snapshot-dependencies/ ./
COPY --from=build-java /app/build/libs/application/ ./
COPY templates /opt/ceh-catalogue/templates
COPY --from=build-web /app/src/css /opt/ceh-catalogue/static/css
COPY web/src/img /opt/ceh-catalogue/static/img
COPY --from=build-permissions web/permission/dist/permission-app.js /opt/ceh-catalogue/static/scripts/permission-app.js
COPY --from=build-catalogue web/catalogue/dist/catalogue-app.js /opt/ceh-catalogue/static/scripts/catalogue-app.js
COPY --from=build-catalogue web/catalogue/dist/9e7d2efc7b95d476f73e.gif /opt/ceh-catalogue/static/scripts/9e7d2efc7b95d476f73e.gif
COPY --from=build-simple-upload web/simple-upload/dist/simple-upload-app.js /opt/ceh-catalogue/static/scripts/simple-upload-app.js
COPY --from=build-hubbub web/hubbub/dist/hubbub-app.js /opt/ceh-catalogue/static/scripts/hubbub-app.js
COPY --from=build-web /app/src/scripts/main-out.js /opt/ceh-catalogue/static/scripts/main-out.js
COPY --from=build-web /app/src/scripts/upload-out.js /opt/ceh-catalogue/static/scripts/upload-out.js
COPY --from=build-web /app/src/vendor/font-awesome-5/webfonts /opt/ceh-catalogue/static/vendor/font-awesome-5/webfonts
COPY --from=build-web /app/src/vendor/requirejs/require.js /opt/ceh-catalogue/static/vendor/requirejs/require.js
RUN chown spring:spring -R /app \
 && chown spring:spring -R /opt/ceh-catalogue \
 && chown spring:spring -R /var/ceh-catalogue \
 && chown spring:spring -R /var/upload
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

