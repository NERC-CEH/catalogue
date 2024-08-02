# Introduction to the catalogue

## Functionality

* Search
* View metadata
* Metadata editor
* Map viewer
* Maintenance - Reindex, Re-link, SPARQL query
* User permissions
* Multi-catalogue - one application instance can run catalogues for lots of different projects, e.g. EIDC, ASSIST, ERAMMP, etc

## Concepts

* DocumentTypes - Multiple document types: ISO 19115, CEH Model, Environmental Modelling facilities, project specific.
* Searching - faceted search, multiple filters, project specific filters
* Linking documents - multiple relationships between documents e.g. dataset and service, monitoring facility and activity
* Multiple representations of metadata e.g. HTML, INSPIRE XML, Json, JSON-LD, Datacite XML, citation XML, RDF turtle
* Services e.g. Datacite DOI minting, INSPIRE metadata harvesting (web accessible folder)
* Version controlled metadata documents using git

## Architecture, Technology & code

### Architecture

A number of Docker containers deployed to an EIP server or a Kubernetes cluster.

Containers:
* Catalogue - Java application
* Mapserver - Apache and Mapserver powering Web Map Service (WMS)
* Proxy - Java application for user authentication and authorisation
* Solr - Apache Solr powers search
* nginx - Handles remote-user header requests

Deployments:
* EIP - Puppet configuration
* Kubernetes - Flux managed Kubernetes resources

## Serverside Technology
Some of the major libraries used, build.gradle list all dependencies.

* Spring Framework - [general website](https://spring.io), [documentation](https://spring.io/projects/spring-framework#learn)
* Spring Security - [documentation](https://spring.io/projects/spring-security)
* Freemarker templating - generating HTML, XML, etc from Java objects [documentation](https://freemarker.apache.org/)
* Jackson - marshalling & unmarshalling Java objects to json [documentation](https://github.com/FasterXML/jackson)
* Lombok - reducing boilerplate code [documentation](https://projectlombok.org/features/all)
* CEH Java Commons - user authentication, Git repository [documentation](https://github.com/NERC-CEH/java-commons)

### Testing

Mainly unit testing with JUnit, Mockito and Spring. Isolate one class and test.
Some integration tests testing the whole application in the Browser and Rest API using Selenium.

* JUnit4 [documentation](https://junit.org/junit4/)
* Mockito - mocking framework [documentation](https://site.mockito.org/)
* Selenium - Browser and REST API testing
* Spring Test - testing HTTP requests and responses

### Build Technology
* Gradle - Java
* docker-compose - Building & running containers

## Clientside technology
All dependencies are in bower.json, some of the major ones are:

* Coffeescript - Modernising javascript (but not! overtaken by ES6 and newer) [documentation](https://coffeescript.org/)
* Backbone - models, views and templates for javascript [documentation](https://backbonejs.org/)
* underscore - utility library [documentation](https://underscorejs.org/)
* require-js - javascript module loader (overtaken by ES6+ imports) [documentation](https://requirejs.org/)
* Bootstrap 3 - layout & styling [documentation](https://getbootstrap.com/docs/3.3/)
* Openlayers - maps [documentation](https://openlayers.org/two/)
* Jquery
* LESS - CSS pre-processor [documentation](http://lesscss.org/)

### Testing
* Jasmine [documentation](https://jasmine.github.io/)

### Build Technology
* Grunt - Javascript [documentation](https://gruntjs.com/)
* Bower - dependency management (deprecated)

## Code

### Spring Framework Concepts

* Inversion of Control (IOC) - the framework calls your code at the right time/place
* Dependency Injection (DI) - the framework provides dependencies to your class
* Container - the  environment (big bucket on Beans) that Spring creates to run the catalogue. Out of which comes the the Inversion of Control and Dependency Injection.
* Beans - framework instantiated objects, either explicitly created with @Bean to implicit through other annotations e.g. @Controller
* Spring MVC - Spring library to handle HTTP requests [documentation](https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html)

### Java packages

#### config
Directly instantiated components (Beans)

#### controllers
HTTP endpoints

#### converters

Conversion from XML to Java objects.

Annotation code to specify which Freemarker template to use to convert Java object to HTML, XML, etc

#### ef
Java classes representing Environmental Facilities i.e Activities, Facilities, Programmes, Networks

#### erammp
Java classes representing ERAMMP project specific things

#### gemini
Java classes representing INSPIRE ISO19115 datasets and services

#### imp
Java classes representing IMP models and applications

#### indexing
Solr and Jena indexing - extracting data from the documentTypes to put into Solr index for searching or Jena for links between documents.

#### model
Classes representing catalogue wide information, general dumping ground for documentType classes without a home elsewhere.

#### ogc
Web Map Service (WMS) model code

#### osdp
Java classes representing Open Soil Data Platform (OSDP) project concepts, same basis as EF but extended.

#### postprocess
Adding information to the Java object in the HTTP request/response cycle. After the controller has retrieved the metadata document but before sent back to the client, adds things like citation information, uses services to add e.g. citation information and links to the object so can be used by Freemarker to render HTML, XML templates

#### publication
Controls how a metadata document can be moved through the publication workflow, i.e. draft -> pending -> published

#### quality
Metadata quality checker, applies rules to the metadata document especially Gemini documents, reports errors with the document

#### repository
How metadata documents are stored and retrieved from a Git repository

#### sa
Sample Archive model documents

#### search
Create search queries to send to Solr, format results returned from Solr

#### services
Lots of services that do stuff.

Decoupling the work of actually doing something from the HTTP code to ease testing and developer comprehension.

#### sparql
Retrieve vocabularies from a SPARQL endpoint like the [UKCEH Vocabulary Service](https://vocabs.ceh.ac.uk)

#### upload
Upload data to the Storage Area Network (SAN), interacts with Hubbub to specify where files should be moved to.

#### util
stuff!

#### validation
Validates Gemini documents against the ISO19115 XSD schema

### Solr
`solr/documents/conf/managed-schema` the Solr index configuration i.e what fields are indexed by the catalogue.

### Client-side

#### less
LESS css for different catalogues, most just import `style-ceh.less` and change a few colours.

#### Apps
* Search - the search page for each catalogue, facets, spatial search, show search results
* Permissions - change users permissions on a metadata record
* Editors - create and edit the different document types e.g. Gemini, OSDP. Configured from components, e.g. simple textbox, list of strings, list of keywords.
* Map Viewer - Show metadata records spatial services, WMS
