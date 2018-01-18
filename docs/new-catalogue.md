# How to Build a New Catalogue

# Step 0: Name

First: `catalogue` not `catalog`, we are not American

# Step 1: ServiceConfig

You need to add your new Catalogue to the catalogue service config

In the function `catalogueService`

You will see `return new InMemoryCatalogueService`

Add a new Catalogue e.g.

```java
Catalogue.builder()
  .id("your-id")
  .title("Your Title")
  .url("http://www.ceh.ac.uk")
  .facetKey("resourceType")
  .documentType(documentType)
  .fileUpload(false)
  .build()
```

* `id`
  * the url e.g. `http://location:8080/your-id/documents"
  * identiying the document
* `title`
  * self explanitory
* `url`
  * no idea but you need it
* `facetKey`
  * you can have chain of these e.g. `.facetKey("keyA").facetKey("keyB")`
  * the values live here `SolrIndex` and have a look in codelist.properties
  * these are filters in your documents view
* `documentType`
  * a list of documents you can create and edit (see documents below)
  * you can have chain of these e.g. `.documentType(documentTypeA).documentType(documentTypeB)`
* `fileUpload`
  * if true then you can upload documents, you see this when you go to <http://location:8080/your-id/documents> and click `create` then select `file-upload`

### Search facets
There are many fields already indexed in Solr that can be used as search facets.

- *resourceType* type of metadata e.g. Dataset, Service, Model, Activity, Facility
- *licence* is the metadata Open Government licenced

#### New search facets

If the existing index fields are not suitable a new index field can be added.

The content of a search facet needs to come from a vocabulary,  [CEH Vocabularies](http://vocabs.ceh.ac.uk/) is a good home.
The metadata records need to be tagged with the keywords.

The facetKey needs to be added to the [Solr schema](../solr/config/documents/conf/schema.xml) and the  [SolrIndex](../java/src/main/java/uk/ac/ceh/gateway/catalogue/indexing/SolrIndex.java)

The documents need to be indexed for each document type in the catalogue e.g. [SolrIndexGeminiDocumentGenerator](../java/src/main/java/uk/ac/ceh/gateway/catalogue/indexing/SolrIndexGeminiDocumentGenerator.java) for Gemini documents and [SolrIndexImpDocumentGenerator](../java/src/main/java/uk/ac/ceh/gateway/catalogue/indexing/SolrIndexImpDocumentGenerator.java) for models / model applications. 

- Add the url of the vocabulary
- Set the newly added field of the SolrIndex

The new facet needs to be added to [HardcodedFacetFactory](../java/src/main/java/uk/ac/ceh/gateway/catalogue/search/HardcodedFacetFactory.java)

#### Vocabularies for search facets

The url structure of vocabulary keywords should follow this format:

vocabulary base url / facet identifier / keyword identifier

e.g. http://vocabs.ceh.ac.uk/imp/wp/nitrogen

- http://vocabs.ceh.ac.uk/imp/ is the vocabulary base url
- wp/ the facet identifier
- nitrogen the keyword identifier

See [SolrIndexGeminiDocumentGenerator](../java/src/main/java/uk/ac/ceh/gateway/catalogue/indexing/SolrIndexGeminiDocumentGenerator.java) for how this is used.


# Step 2: Users

you need to create some new roles for development

first in `DevelopmentUserStoreConfig`

You will want to add 2 roles, an editor and a publisher

at the top of this file add

```java
public static final String YOUR_CATALOGUE_EDITOR = "role_yourCatalogue_editor";
public static final String YOUR_CATALOGUE_PUBLISHER = "role_yourCatalogue_publisher";
```

second you need a new user, add something like this

```java
@Bean @Qualifier("yourCatalogue-publisher")
public CatalogueUser yourCataloguePublisher() throws lreadyTakenException {
    CatalogueUser publisher = new CatalogueUser()
        .setUsername("yourCatalogue-publisher")
        .setEmail("yourCatalogue-publisher@ceh.ac.uk");
    groupStore().grantGroupToUser(publisher, YOUR_CATALOGUE_PUBLISHER);
    groupStore().grantGroupToUser(publisher, YOUR_CATALOGUE_EDITOR);
    userStore().addUser(publisher, "publisherpassword");
    return publisher;
}
```

You also need to add the new user to the group store in `groupStore`, using the title from `Step 1: ServiceConfig`

```java
toReturn.createGroup(YOUR_CATALOGUE_PUBLISHER, "Your Title Publisher Role");
toReturn.createGroup(YOUR_CATALOGUE_EDITOR, "Your Title Editor Role");
```

now when you go to <http://location:8080/your-id/documents> you can add the header `Remote-User=yourCatalogue-publisher` and they can read/write/edit files

# Step 3: View

in the `Gruntfile.js` you need to add the following in the `less` task

```js
youId: {
  files: {
    'src/css/style-youId.css': 'src/less/style-youId.less'
  }
}
```

and in the `cssmin` task

```js
youId: {
  files: {
    'src/css/style-youId.css': 'src/css/style-youId.css'
  }
}
```

and you need to create the two files `src/less/style-youId.less` and `src/less/style-youId.css` (the latter triggers grunt when running concurrently, but not strictly required)

your less files will look like this

```less
@import "style-ceh";
```

but if you want your own colors you can do something like this

```less
@brand-success: #3498DB;
@header-text: #ECF0F1;
.text-red {color:#E74C3C;}
```

# Step 4: Documents

this is going to show requirements for making/editing and assigning permissions to document(s)

you need at least one document otherwise your catalogue is a waste of breath

if you want to see more then read [new document type](newDocumentType.markdown)

# Step 4a: Document

create a new folder `uk.ac.ceh.gateway.catalogue.your-id` add your document to this folder e.g.

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
    @Template(called="html/your-id/your-document.html.tpl", whenRequestedAs= MediaType.TEXT_HTML_VALUE)
})
public class YourDocument extends AbstractMetadataDocument {
}
```

# Step 4b: html file

create `html/your-id/your-document.html.tpl`

which should have 

```html
<#import "../skeleton.html.tpl" as skeleton>

<!-- You need catalogue=catalogues.retrieve(metadata.catalogue) to load your css file -->
<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>

<!-- You need id="metadata" to wrap the whole document so that everything works e.g. _admin.html.tpl buttons will work -->
<div id="metadata">
  <div class="container">
    <!-- put data here e.g. -->
    <h1>${title}</h1>
  </div>
</div>
</@skeleton.master>
```

you can get access to your values of the focument on the root level, in the above example `title` is a value on the document

Anything else is up to you

# Step 4c: WebConfig

you need to add this to the top

```java
public static final String YOUR_DOCUMENT_JSON_VALUE = "application/vnd.your-document+json";
public static final String YOUR_DOCUMENT_SHORT = "your-document";
```

then in `configureContentNegotiation` you need to add

`.put(YOUR_DOCUMENT_SHORT, MediaType.parseMediaType(YOUR_DOCUMENT_JSON_VALUE))` 

# Step 4d: ServiceConfig

in `metadataRepresentationService` you need to add

`.register(YOUR_DOCUMENT_SHORT, YourDocument.class);`

in `catalogueService` you need to create a document e.g.

```java
DocumentType yourDocument = DocumentType.builder()
    .title("Document Title")
    .type(YOUR_DOCUMENT_SHORT)
    .build();
```

which your catalogue can now accept e.g.

```java
Catalogue.builder()
  .id("your-id")
  .title("Your Title")
  .url("http://www.ceh.ac.uk")
  .facetKey("resourceType")
  .documentType(yourDocument)
  .fileUpload(false)
  .build()
```

you also need to add conversters for your documents in `messageConverters` add

```java
converters.add(new Object2TemplatedMessageConverter<>(YourNewDocument.class, freemarkerConfiguration()));
```

Now your document will render as per `html/your-id/your-document.html.tpl`

# Step 4e: Controller

You need this to save and view a file

```java
package uk.ac.ceh.gateway.catalogue.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uk.ac.ceh.components.userstore.springsecurity.ActiveUser;
import uk.ac.ceh.gateway.catalogue.your-id.YourDocument;
import uk.ac.ceh.gateway.catalogue.model.CatalogueUser;
import uk.ac.ceh.gateway.catalogue.model.MetadataDocument;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepository;
import uk.ac.ceh.gateway.catalogue.repository.DocumentRepositoryException;

import static uk.ac.ceh.gateway.catalogue.config.WebConfig.*;

@Controller
public class YourController extends AbstractDocumentController {

  @Autowired
  public YourController(DocumentRepository documentRepository) {
    super(documentRepository);
  }

  @PreAuthorize("@permission.userCanCreate(#catalogue)")
  @RequestMapping(value = "documents", method = RequestMethod.POST, consumes = YOUR_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument> newYourDocument(@ActiveUser CatalogueUser user, @RequestBody YourDocument document,
      @RequestParam("catalogue") String catalogue) throws DocumentRepositoryException {
    return saveNewMetadataDocument(user, document, catalogue, "new Your Document");
  }

  @PreAuthorize("@permission.userCanEdit(#file)")
  @RequestMapping(value = "documents/observable/{file}", method = RequestMethod.PUT, consumes = YOUR_DOCUMENT_JSON_VALUE)
  public ResponseEntity<MetadataDocument> saveYourDocument(@ActiveUser CatalogueUser user, @PathVariable("file") String file,
      @RequestBody YourDocument document) throws DocumentRepositoryException {
    return saveMetadataDocument(user, file, document);
  }
}
```

# Step 4f: View

in `Main.coffee` in `initEditor` you need to add

```coffee
'your-document':
    View: YourDocumentView
    Model: EditorMetadata
    mediaType: 'application/vnd.your-document+json'
```

then create `YourDocumentView.coffee` e.g.

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

**IMPORTANT** you need a title and resource type for your documents

we have hard coded this type using `@model.set('type', 'dataset') unless @model.has('type')`

if your document can be of varying types add this to `views: []`

```coffee
new SingleObjectView
    model: @model
    modelAttribute: 'resourceType'
    ModelType: ResourceType
    label: 'Resource Type'
    ObjectInputView: ResourceTypeView
```

# Step 4g: Admin

you don't have to do this step if you don't want to. It lets you let users edit and publish the document

create `html/your-id/_admin.html.tpl`

```html
<#if permission.userCanEdit(id)>
  <div id="adminPanel" class="panel hidden-print">
    <div class="panel-heading"><p class="panel-title">Admin</p></div>
    <div class="panel-body">
      <div class="btn-group btn-group-sm btn-group-justified" role="group">
        <a href="#" class="btn btn-default edit-control" data-document-type="${metadata.documentType}">Edit</a>
        <div class="btn-group btn-group-sm" role="group">
            <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">Publish <span class="caret"></span></button>
              <ul class="dropdown-menu dropdown-menu-right">
                <li><a href="/documents/${id?html}/permission">Amend permissions</a></li>
                <li><a href="/documents/${id?html}/publication">Publication status</a></li>
                <li><a href="/documents/${id?html}/catalogue" class="catalogue-control">Amend catalogues</a></li>
              </ul>
          </div>
      </div>
    </div>
  </div>
</#if>
```

then in `html/your-id/your-document.html.tpl`

```html
<#import "../skeleton.html.tpl" as skeleton>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>

<div id="metadata">
   <div class="container">
    <#include "_admin.html.tpl">
    </div>
  </div>
</@skeleton.master>
```
