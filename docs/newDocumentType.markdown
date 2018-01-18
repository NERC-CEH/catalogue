# Adding a new document type

## Object Model

* add new Java object model that extends AbstractMetadataDocument. If a single documentType add to u.a.c.g.c.model package, if more create a package.
* link to Freemarker template through @ConvertUsing and @Template annotations.
* try and reuse classes in the u.a.c.g.c.model & u.a.c.g.c.gemini packages before creating new classes.
* implement Solr indexing interfaces e.g. WellKnownText for location indexing

## Freemarker Templates for serverside rendering

* new template to render object model to html
* any other templates for xml, rdf as required

## WebConfig

* register new media type string, "application/vnd.{documentType}+json"
* register new short name, "{documentType}"
* add mediaType to configureContentNegotiation()

## ServiceConfig

* register template in messageConverters()
* register document type in metadataRepresentationService()

## CatalogueServiceConfig

* create new documentType in catalogueService() and register with a catalogue

## DocumentController

If creating many documentTypes consider creating a new documentController to group them together.

* add new{documentType} method
* add update{documentType} method

## Solr Indexing

* indexing is covered by SolrIndexMetadataDocumentGenerator
* spatial location is covered by implementing the WellKnownText interface
* new indexing requirements should follow WellKnownText as an example 

## Jena Indexing

* new JenaIndex{documentType}Generator to index fields not covered by JenaIndexMetadataDocumentGenerator

## Editor

### New editor

  * configure a new editor extending EditorView

### Create Dropdown
* Main.coffee - add document-type to initEditor LOOKUP (needs to match ServiceConfig.java metadataRepresentationService() and catalogueService())

## codelist.properties

* add resource type to metadata.resourceType list
