# How to Build a New Catalogue

## Step 1: Add a catalogue

You need to add your new Catalogue to the `CatalogueServiceConfig.java`

There are now different `CatalogueService` for different servers the catalogue is deployed on.
Decide which server your catalogue will be deployed on, probably EIDC.

In the function `eidCatalogue` you will see `return new InMemoryCatalogueService`

Add a new Catalogue (keep in alphabetical order) e.g.

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

* *id*
  * identifies the search page of this catalogue e.g. `http://location:8080/your-id/documents`
* *title*
  * self-explanatory
* *url*
  * URL to a project website
* *facetKey*
  * you can have a chain of these e.g. `.facetKey("keyA").facetKey("keyB")`
  * the values live here `SolrIndex` and have a look in codelist.properties
  * these are filters in your search page
* *documentType*
  * a list of documents you can create and edit (see documents below)
  * you can have a chain of these e.g. `.documentType(documentTypeA).documentType(documentTypeB)`
* *fileUpload*
  * if `true` then you can upload documents, you see this when you go to <http://location:8080/your-id/documents> and click `create` then select `file-upload`.
  Most of the time it should be `false`

### Search facets
There are many fields already indexed in Solr that can be used as search facets.

* *resourceType* type of metadata e.g. Dataset, Service, Model, Activity, Facility
* *licence* is the metadata Open Government licenced

#### New search facets

If the existing index fields are not suitable a new index field can be added.

The content of a search facet needs to come from a vocabulary,  [UKCEH Vocabularies](http://vocabs.ceh.ac.uk/) is a good home.
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


## Step 2: Add Users & Groups

You need to create some new roles for development.

First, in `DevelopmentUserStoreConfig` add two roles, an editor and a publisher

at the top of this file add

```java
public static final String YOUR_CATALOGUE_EDITOR = "role_yourCatalogue_editor";
public static final String YOUR_CATALOGUE_PUBLISHER = "role_yourCatalogue_publisher";
```

Second, you need a new user, add something like this

```java
@PostConstruct
public void yourCataloguePublisher() throws UsernameAlreadyTakenException {
  val user =new CatalogueUser()
    .setUsername("yourCatalogue-publisher")
    .setEmail("yourCatalogue-publisher@ceh.ac.uk");
  addUserToGroup(user, YOUR_CATALOGUE_EDITOR, YOUR_CATALOGUE_PUBLISHER);
  userStore().addUser(user, "password");
}
```

You also need to add the new groups to the group store in `groupStore`, using the title from `Step 1: CatalogueServiceConfig`

```java
createGroup(YOUR_CATALOGUE_EDITOR, "");
createGroup(YOUR_CATALOGUE_PUBLISHER, "");
```

Now when you go to <http://location:8080/your-id/documents> you can add the header `Remote-User=yourCatalogue-publisher` and they can read/write/edit files.
Use a browser plugin like [ModHeaders](https://bewisse.com/modheader/) to modify the headers.

## Step 3: Add style

In the `webpack.scss.js` you need to add the following in the `entry`

```js
entry: {
      'style-youId': './scss/style-youId.scss'
}
```

You need to create the file `src/scss/style-youId.scss`

your `scss` files will look like this

```scss
@import "style-ceh";
```

but if you want your own colors you can add something like this

```scss
$brand-success: #3498DB;
$header-text: #ECF0F1;
.text-red {color:#E74C3C;}
```

## Step 4: Adding new DocumentTypes

__N.B.__ This step is only needed if adding a new documentType, if re-using existing documentTypes
this step can be ignored.

See [creating a new document type](newDocumentType.md) for details.
