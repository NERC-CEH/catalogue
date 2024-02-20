# Searching with the User Interface

## Generic Text Search

By default, all text entered into the search box will search all fields indexed
by Solr. This is especially useful when searching for data in specific topics
that are likely to be mentioned in the title or description both of which are
visible from the search screen.

### Basic Search

By default all indexed fields are searched when a user simply enters text
into the search box. This can be useful in particular when the search terms
are contained either in in the record title or description.

Below are a few examples of how this default search operates.

|              | Example            | Explanation |
|--------------|--------------------|-------------|
| Text Match   | `soil chemistry`   | Searches for records containing either the word soil or the word chemistry |
| Phrase Match | `"soil chemistry"` | Searches for records containing the phrase "soil chemistry" |

This can be combined with basic conditions (AND/OR/NOT) to further filter
records based on content.

| Search Term | Example                | Explanation                                                                                 |
|-------------|------------------------|---------------------------------------------------------------------------------------------|
| OR          | `soil OR chemistry`    | Searches for records containing either the word soil or the word chemistry                  |
| AND         | `"soil AND chemistry"` | Searches for records containing both the word soil and the word chemistry                  |
| NOT         | `"soil NOT chemistry"` | Searches for records containing the word soil but which does not contain the word chemistry |

## Advanced - Solr Searching

The mechanism powering the search used in the Catalogue is called [Solr](https://solr.apache.org/),
this provides a number of powerful tools to search the records for specific data. In
simple cases this allows you to search specific fields but can be used for more
complex purposes.

Queries can be specified using the [Lucene](https://lucene.apache.org/core/2_9_4/queryparsersyntax.html)
query syntax. Below are documented some simple examples.

### Searchable Fields

Depending on the document/record in question a wide range of data may be queryable.
Below is a non-exhaustive list of fields which are generally widely used.

- Title - `title`
- Author - `authorName`
- Funder - `funder`
- Grant - `grant`
- Incoming Citation Count - `incomingCitationCount`
- Spatial Information `locations`

A larger list of fields which may not all be searchable can be found [here](/java/src/main/java/uk/ac/ceh/gateway/catalogue/indexing/solr/SolrIndex.java).

### Simple Field Lookup

Records can be returned that contain a specific field using the `fieldName:*`
query. Alternatively if you want to search a field for a specific text this can be
specified e.g. `fieldName:text`.

For example the following query would return records with both funder and author
with a provided ORCID present.

`authorOrcid:* AND funder:*`

### Spatial Search

Lucene also allows records to be [queried spatially](https://lucene.apache.org/core/9_5_0/spatial-extras/org/apache/lucene/spatial/query/SpatialOperation.html).
This is useful when exploring datasets using bounding boxes or polyons. Typical
queries include but are not limited to;

- IsWithin - (OGC Definition)
- Intersects - (OGC Definition)

Examples of the queries can be found below;

#### Search for datasets within a Polygon of Wales
`locations:"IsWithin(POLYGON((-5.47 53.43, -2.65 53.43, -2.65 51.38, -5.47 51.38, -5.47 53.43)))"`

#### Search for datasets that intersect with a Polygon of Scotland
`locations:"Intersects(POLYGON((-8.65 60.87, -0.73 60.87, -0.73 54.63, -8.65 54.63, -8.65 60.87)))"`
