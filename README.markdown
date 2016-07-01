# CEH Catalogue

This is the CEH metadata catalogue project. It is built using Docker.

## Project Structure

The Project is built up of a few different parts:

- **/web**        - Location of the web component of the project, this is mainly `coffeescript` and `less` style sheets
- **/java**       - Standard `maven` project which powers the server side of the catalogue
- **/templates**  - `Freemarker` templates which are used by the `java` application for generating the different metadata views
- **/schemas**    - XSD Schemas which are used to validate the various output xml files
- **/solr**       - `Solr Server` web application, this handles the free-text indexing and searching of the application
- **/mapserver**  - `Map Server` web application which is responsible for hosting WMS's from metadata records
- **/fixtures**   - Test data used by the `rspec` suite
- **/spec**       - RSpec end-to-end test suite

## Getting started (With Docker)

The catalogue requires a few system libraries to be installed in order to aid development. These are:

- Git
- Docker
- Make

Having installed these you can then build the catalogue code base. Re-Run this command any time you want to make changes to java code or a docker configuration.

    make build

To develop against the code base, you can run `make develop`. This will construct an environment which compiles LESS stylesheets and responds to changes to the *templates*.

`make test-data` will populate a repository of test-data. Re-running `make build` will create a catalogue which is powered by this repository.

### Selenium Testing (Docker)

The project contains an `rspec` suite of selenium tests. These can be executed using the `make selenium` command. This will create the browsers required for testing in docker containers and run through the test suite.

## Getting started (With Vagrant)

If you don't have docker installed natively, you can use the provided Vagrant box to get the project going. The vagrant box will create a docker environment so that you can execute the standard `make` commands.

To get the project running. Install :

* vagrant
* vagrant_vmware plugin

Then in a bash window run. This will provision a fresh vm with Docker and perform a `make build`

    vagrant up

To execute the different `make` commands you will need to ssh on to the vagrant machine.

    vagrant ssh
    # Now inside the vagrant machine
    cd /vagrant
    make develop

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

## Map Viewer

All requests for maps go through our catalogue api as TMS coordinates (i.e. z, x, y). When a map request comes in, the catalogue api transforms the z, x, y coordinates into a wms GetMap request in the EPSG:3857 projection system. This is the projection system which is used by Google Maps style web mapping applications.

The Catalogue api will gracefully handle certain upstream mapping failures. These failures will be represented as images so that they can be displayed by the normal mapping application.

Below are the images which are displayed and there meaning:

## Legend not found
![Legend not found](java/src/main/resources/legend-not-found.png) 

Displayed when a Legend image is requested but one has not been specified in the GetCapabilities

## Upstream Failure
![Upstream Failure](java/src/main/resources/proxy-failure.png) 

The call to the server failed for some unspecified reason, this may be because the connection failed.

## Invalid response
![Invalid response](java/src/main/resources/proxy-invalid-response.png) 

The upstream service returned some content, but it was not in the format which was expected. It maybe that the upstream service replied with an error message rather than an image.

## Invalid Resource
![Invalid Resource](java/src/main/resources/proxy-invalid-resource.png) 

The wms get capabilities returned a malformed reference to either a GetLegend or GetMap url. This can happen if you are using a buggy web map server or an corrupt external get capabilities.