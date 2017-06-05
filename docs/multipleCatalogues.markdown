# Adding another catalogue

## Configure a catalogue

Add a new catalogue to [ServiceConfig](../java/src/main/java/uk/ac/ceh/gateway/catalogue/config/ServiceConfig.java).catalogueService()

- *id* short identifier for catalogue, will be used in url to catalogue's search page i.e. /{id}/documents
- *title* title of catalogue
- *url* url of project page, will be used in the header as a link
- *facetKey* names of search facets to use in the catalogue search page. A facetKey must match a field name in the [Solr schema](../solr/config/documents/conf/schema.xml)
- *documnetType* names of document types to use in the catalogue
- *fileUpload* **true** if file upload allowed 

## Search facets
There are many fields already indexed in Solr that can be used as search facets.

- *resourceType* type of metadata e.g. Dataset, Service, Model, Activity, Facility
- *licence* is the metadata Open Government licenced

### New search facets

If the existing index fields are not suitable a new index field can be added.

The content of a search facet needs to come from a vocabulary,  [CEH Vocabularies](http://vocabs.ceh.ac.uk/) is a good home.
The metadata records need to be tagged with the keywords.

The facetKey needs to be added to the [Solr schema](../solr/config/documents/conf/schema.xml) and the  [SolrIndex](../java/src/main/java/uk/ac/ceh/gateway/catalogue/indexing/SolrIndex.java)

The documents need to be indexed for each document type in the catalogue e.g. [SolrIndexGeminiDocumentGenerator](../java/src/main/java/uk/ac/ceh/gateway/catalogue/indexing/SolrIndexGeminiDocumentGenerator.java) for Gemini documents and [SolrIndexImpDocumentGenerator](java/src/main/java/uk/ac/ceh/gateway/catalogue/indexing/SolrIndexImpDocumentGenerator.java) for models / model applications. 

- Add the url of the vocabulary
- Set the newly added field of the SolrIndex

The new facet needs to be added to [HardcodedFacetFactory](../java/src/main/java/uk/ac/ceh/gateway/catalogue/search/HardcodedFacetFactory.java)

### Vocabularies for search facets

The url structure of vocabulary keywords should follow this format:

vocabulary base url / facet identifier / keyword identifier

e.g. http://vocabs.ceh.ac.uk/imp/wp/nitrogen

- http://vocabs.ceh.ac.uk/imp/ is the vocabulary base url
- wp/ the facet identifier
- nitrogen the keyword identifier

See [SolrIndexGeminiDocumentGenerator](../java/src/main/java/uk/ac/ceh/gateway/catalogue/indexing/SolrIndexGeminiDocumentGenerator.java) for how this is used.

## Crowd groups

Each catalogue needs two groups in [Crowd](https://crowd.ceh.ac.uk)

- *ROLE_{id}_EDITOR* users who can create new metadata records
- *ROLE_{id}_PUBLISHER* users who can make metadata records public

Where *{id}* is the catalogue id

## Style

The catalogue can have it's own style. A Less file should import the base [ceh style](../web/src/less/style-ceh.less). The file should be named style-{id}.less where {id} is the catalogue id.

Modify the [Gruntfile](../web/Gruntfile.coffee) adding the catalogue to the less and cssmin targets.

## Document Type

Existing document types can be used in the catalogue or a new document type developed, [instructions here](newDocumentType.markdown).