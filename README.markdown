# CEH Catalogue

The CEH metadata catalogue project.

## Project Structure

- **/docs**       - Documentation
- **/web**        - Location of the web component of the project, this is mainly `coffeescript` and `less` style sheets
- **/java**       - Standard `gradle` project which powers the server side of the catalogue
- **/templates**  - `Freemarker` templates which are used by the `java` application for generating the different metadata views
- **/schemas**    - XSD Schemas which are used to validate the various output xml files
- **/solr**       - `Solr Server` web application, this handles the free-text indexing and searching of the application
- **/mapserver**  - `Map Server` web application which is responsible for hosting WMS's from metadata records
- **/fixtures**   - Test data used by the `rspec` suite
- **/spec**       - RSpec end-to-end test suite

**NB:** `web/src/vendor/requirejs` needs to be left alone otherwise the build breaks

## Usernames and Passwords

you will need to create a `secrets.env` file with the following

```
JIRA_USERNAME=jira_username
JIRA_PASSWORD=jira_password
CROWD_USERNAME=eip-ro
CROWD_PASSWORD=FindMeInK33Pa55
```

## Getting started

The catalogue requires a few tools:

- Git
- Docker
- Docker Compose

Having installed these you can then build the catalogue code base by running:

    ./gradlew

the EIDC catalogue is then available on:

    http://localhost:8080/eidc/documents

## Developing Javascript & LESS

    ./gradlew :web:grunt_concurrent

will run a process that watches the javascript and less directories and recompiles the files on an changes.

## Selenium Testing

The project contains an `rspec` suite of selenium tests.
These can be executed using

    SELENIUM_TEST=true ./gradlew selenium

This will create the browsers required for testing in docker containers and run through the test suite.

## Multiple Catalogues

Multiple catalogues are supported by this software.

A catalogue has it's own:
- search page
- style
- editor and publisher groups
- metadata records
- document types

New catalogues can be added, [instructions here](docs/multipleCatalogues.markdown).

## Catalogue Content

A catalogue can reuse existing metadata content by linking to public metadata in another catalogue using the Link document type.

![Link document type](docs/link.png)

## Remote-User

The catalogue is designed to sit behind a **Security Proxy** (see [RequestHeaderAuthenticationFilter](http://docs.spring.io/autorepo/docs/spring-security/3.2.0.RELEASE/apidocs/org/springframework/security/web/authentication/preauth/RequestHeaderAuthenticationFilter.html) which acts as the authentication source for the application. Therefore, the catalogue will respond to the `Remote-User` header and handle requests as the specified user.

To simplify development, the `DevelopmentUserStoreConfig` is applied by default. This creates some dummy users in various different groups which you can masquerade as. The simplest way to do this is use a browser extension which applies the `Remote-User` header. I recommend **ModHeader for chrome**.

Then set the a request header:

    Remote-User: superadmin

Other available users are:

- superadmin
- bamboo
- readonly
- editor
- publisher
- admin

Also, be sure to go to http://foo.ceh.ac.uk:8080/documents rather than http://localhost:8080/documents, which needs an edit to your hosts file:

eg 127.0.0.1       localhost foo.ceh.ac.uk

## Map Viewer

All requests for maps go through our catalogue api as TMS coordinates (i.e. z, x, y). When a map request comes in, the catalogue api transforms the z, x, y coordinates into a wms GetMap request in the EPSG:3857 projection system. This is the projection system which is used by Google Maps style web mapping applications.

The Catalogue api will gracefully handle certain upstream mapping failures. These failures will be represented as images so that they can be displayed by the normal mapping application.

Below are the images which are displayed and there meaning:

### Legend not found
![Legend not found](java/src/main/resources/legend-not-found.png)

Displayed when a Legend image is requested but one has not been specified in the GetCapabilities

### Upstream Failure
![Upstream Failure](java/src/main/resources/proxy-failure.png)

The call to the server failed for some unspecified reason, this may be because the connection failed.

### Invalid response
![Invalid response](java/src/main/resources/proxy-invalid-response.png)

The upstream service returned some content, but it was not in the format which was expected. It maybe that the upstream service replied with an error message rather than an image.

### Invalid Resource
![Invalid Resource](java/src/main/resources/proxy-invalid-resource.png)

The wms get capabilities returned a malformed reference to either a GetLegend or GetMap url. This can happen if you are using a buggy web map server or an corrupt external get capabilities.