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
* register Solr IndexGenerator in documentIndexingService()

## DocumentController

* add new{documentType} method
* add update{documentType} method

## Indexing

* new SolrIndex{documentType}Generator to index fields not covered by SolrIndexMetadataDocumentGenerator

## PostProcessing

## Editor

* configure an editor extending EditorView

## codelist.properties

* add resource type to metadata.resourceType list
