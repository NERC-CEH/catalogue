# Adding a new document type

## Object Model

* add new Java object model
* link to template

## Templates

* new template to render object model to html
* any other templates for xml rdf as required

## WebConfig

* register new media type string, "application/{documentType}+json"
* register new short name, "{documentType}"
* add mediaType to configureContentNegotiation()

## ServiceConfig

* register template in messageConverters()
* register document type in metadataRepresentationService()
* register Solr IndexGenerator in documentIndexingService() (if doing additional indexing)
* create new documentType in catalogueService() and register with a catalogue

## DocumentController

* add new{documentType} method
* add update{documentType} method

## Indexing

* new SolrIndex{documentType}Generator to index fields not covered by SolrIndexMetadataDocumentGenerator

## PostProcessing

## Editor

### New editor

  * configure a new editor extending EditorView

### Create Dropdown
* Main.coffee - add document-type to initEditor LOOKUP (needs to match ServiceConfig.java metadataRepresentationService() and catalogueService())

## codelist.properties

* add resource type to metadata.resourceType list
