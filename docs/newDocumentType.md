# Creating a new Document Type

## Summary
* Java object model
* Freemarker template
* Document type
* Media type
* Controller
* Indexing
  * Solr
  * Jena
* Configuration
* Client-side editor
* Testing

## Java Object Model

Create a new package `uk.ac.ceh.gateway.catalogue.your-id` and add your document to this folder.
* links to Freemarker template through `@ConvertUsing` and `@Template` annotations.
* implement Solr indexing interfaces e.g. `WellKnownText` for location indexing

```java
package uk.ac.ceh.gateway.catalogue.your-id;

import org.springframework.http.MediaType;
import uk.ac.ceh.gateway.catalogue.converters.ConvertUsing;
import uk.ac.ceh.gateway.catalogue.converters.Template;
import uk.ac.ceh.gateway.catalogue.model.AbstractMetadataDocument;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@ConvertUsing({
    @Template(called="html/your-id/your-document.ftl", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class YourDocument extends AbstractMetadataDocument {
    private String field;
}
```

## Freemarker Templates for server-side rendering

* new template to render object model to html
* any other templates for xml, rdf as required
* Spring handles json format 

Create file `html/your-id/your-document.ftl`.

You can get access to the values of the document on the root level, in the example `title` is a value on the document.

```html
<#import "../skeleton.ftl" as skeleton>

<!-- You need catalogue=catalogues.retrieve(metadata.catalogue) to load your css file -->
<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>

<!-- You need id="metadata" to wrap the whole document so that everything works e.g. _admin.ftlh buttons will work -->
<div id="metadata">
    <div class="container">
      <!-- put data here e.g. -->
      <h1>${title}</h1>
  </div>
</div>
</@skeleton.master>
```

Register the template and document type in `extendMessageConverters` method of WebConfig.

### Admin template

To let users edit and publish the document add an`_admin.ftlh` template to the template
in `html/your-id/your-document.ftl`. See `template/html/link/_admin.ftlh`.

```html
<#import "../skeleton.ftl" as skeleton>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>

<div id="metadata">
   <div class="container">
    <#include "_admin.ftl">
    </div>
  </div>
</@skeleton.master>
```

```java
converters.add(new Object2TemplatedMessageConverter<>(YourDocument.class, freemarkerConfiguration()));
```

## Document Type

Add your document type to DocumentTypes.java

```java
public static final String YOUR_DOCUMENT = "your-document";
public static DocumentType YOUR_DOCUMENT_TYPE = DocumentType.builder()
    .title("Your Document Title")
    .type(YOUR_DOCUMENT)
    .build();
```

Register the document type with a catalogue in `CatalogueServiceConfig`

## Media Type

Add your media type to CatalogueMediaTypes.java

```java
public static final String YOUR_DOCUMENT_JSON_VALUE = "application/vnd.your-document+json";
```

### ServiceConfig

Register document type in `metadataRepresentationService`

```java
.register(YOUR_DOCUMENT, YourDocument.class)
```

### DocumentController

Create a new `YourDocumentController` in your document type package

* add `newYourDocument` method - to create a new document
* add `updateYourDocument` method - to update an existing document

```java
@Slf4j
@Controller
@RequestMapping("documents")
public class YourDocumentController extends AbstractDocumentController {

  @Autowired
  public YourDocumentController(DocumentRepository documentRepository) {
    super(documentRepository);
    log.info("Creating")
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @PostMapping(consumes = YOUR_DOCUMENT_JSON_VALUE)
  public ResponseEntity<YourDocument> newYourDocument(
      @ActiveUser CatalogueUser user,
      @RequestBody YourDocument document,
      @RequestParam("catalogue") String catalogue
  ) {
    return saveNewMetadataDocument(user, document, catalogue, "new Your Document");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @PutMapping(value = "{file}", consumes = YOUR_DOCUMENT_JSON_VALUE)
  public ResponseEntity<YourDocument> updateYourDocument(
      @ActiveUser CatalogueUser user,
      @PathVariable("file") String file,
      @RequestBody YourDocument document
  ) {
    return saveMetadataDocument(user, file, document);
  }
}
```

### Solr Indexing

To have fields in the document type be searchable they need to be indexed into Solr.
Many fields will already be indexed through the `MetadataDocument`.

* indexing is covered by `SolrIndexMetadataDocumentGenerator`
* spatial location is covered by implementing the `WellKnownText` interface
* new indexing requirements should follow `WellKnownText` as an example 

## Jena Indexing
New `JenaIndexYourDocumentGenerator` to index fields not covered by 
`JenaIndexMetadataDocumentGenerator`

## Editor

### New editor

Create a new editor extending `EditorView`

```coffee
define [
  'cs!views/EditorView'
  'cs!views/editor/InputView'
], (
  EditorView
  InputView
) -> EditorView.extend

  initialize: ->
    @model.set('type', 'dataset') unless @model.has('type')

    @sections = [
      label: 'Basic Info'
      title: 'Basic Info'
      views: [
          new InputView
            model: @model
            modelAttribute: 'title'
            label: 'Title'
      ]
    ]

    EditorView.prototype.initialize.apply @
```

**IMPORTANT** you need a `title` and  `type` for your document,
we have hardcoded `type` using `@model.set('type', 'dataset') unless @model.has('type')`

### Configure editor

In `Main.coffee` add document-type to `initEditor`, key in `lookup` needs to match
value in `DocumentTypes.java` e.g. `YOUR_DOCUMENT = "your-document";`

```coffee
'your-document':
    View: YourDocumentView
    Model: EditorMetadata
    mediaType: 'application/vnd.your-document+json'
```


## Resource Type

Add resource type to `metadata.resourceType` list in `codelist.properties`
