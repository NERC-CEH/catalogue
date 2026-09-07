@prefix void:    <http://rdfs.org/ns/void#> .
@prefix foaf:    <http://xmlns.com/foaf/0.1/> .
@prefix dcterms: <http://purl.org/dc/terms/> .
@prefix dcat:    <http://www.w3.org/ns/dcat#> .
@prefix prov:    <http://www.w3.org/ns/prov#> .
@prefix sd:      <http://www.w3.org/ns/sparql-service-description#> .

<#--
  Every literal in this document is assembled by hand rather than serialised by
  Jena, so escaping is this template's problem. One unescaped backslash in one
  hand-built literal took every export down for a week (dri-one #344), and a
  line break in a title would break this document the same way -- which the
  backslash-and-quote pair of ?replace calls that used to sit inline at each
  literal did not cover.

  Not templates/rdf/_turtle.ftl's escape(), which substitutes an apostrophe for a
  double quote to stay byte-compatible with years of already-published literals.
  There is nothing published here to stay compatible with, so this escapes the
  quote properly instead.
-->
<#function lit text>
  <#return text?replace('\\', '\\\\')?replace('"', '\\"')?replace('\r', ' ')?replace('\n', ' ')>
</#function>

<${baseUri}/.well-known/void>
    a void:DatasetDescription ;
    dcterms:title "UKCEH Metadata Catalogue VoID Description";
<#list catalogues as cat>
    foaf:topic <${baseUri}/${cat.id}/documents> ;
</#list>
<#-- source.graph() not source.graph: Authority is a record, so its accessors
     do not follow the getX() convention and FreeMarker exposes them as methods. -->
<#list sourceGraphs![] as source>
    foaf:topic <${source.graph()}> ;
</#list>
    .

<#--
  VoID describes datasets but has no way to say which named graph holds one, so
  the graph layout is declared with the SPARQL 1.1 Service Description
  vocabulary — the one place it belongs. Without this a consumer has to be told
  out of band that the endpoint separates what the catalogue asserts from what
  each authority asserts (dri-one #350).

  Note the endpoint's default graph is the union of all of them, so a query with
  no GRAPH clause still sees everything and nothing that worked before changes.
-->
<${sparqlUrl}>
    a sd:Service ;
    sd:endpoint <${sparqlUrl}> ;
    sd:supportedLanguage sd:SPARQL11Query ;
    sd:feature sd:UnionDefaultGraph ;
    sd:defaultDataset [
        a sd:Dataset ;
        sd:defaultGraph [ a sd:Graph ] ;
        sd:namedGraph [
            a sd:NamedGraph ;
            sd:name <${catalogueGraph}> ;
            sd:graph <${catalogueGraph}>
        ] ;
<#list sourceGraphs![] as source>
        sd:namedGraph [
            a sd:NamedGraph ;
            sd:name <${source.graph()}> ;
            sd:graph <${source.graph()}>
        ] ;
</#list>
    ] ;
    .

<${catalogueGraph}>
    a void:Dataset ;
    dcterms:title "Everything the UKCEH catalogue asserts about its own records"@en ;
    dcterms:publisher <https://ror.org/00pggkr55> ;
    void:sparqlEndpoint <${sparqlUrl}> ;
<#list catalogues as cat>
    void:subset <${baseUri}/${cat.id}/documents> ;
</#list>
    .

<#--
  One dataset per authority the catalogue republishes. The catalogue asserts
  nothing of its own in these graphs — they hold the authority's own statements,
  republished unchanged — which is exactly why they are separate graphs rather
  than merged into the catalogue's.

  Title, description, vocabularies and licence all come from the SourceGraph the
  provider declares, which is the same one written into the graph itself as its
  void:Dataset header. They used to be written here instead, and every graph was
  described as SKOS concept labels with a skos:prefLabel partition — true of the
  vocabularies, and of nothing added after them: the DOI, GeoNames, GtR and DEIMS
  graphs hold no SKOS at all and the ORCID graph holds FOAF (dri-one #350).

  A licence is stated only where the authority's terms are established. ORCID,
  ROR and Wikidata release their public records under CC0 and GeoNames under
  CC-BY; the vocabularies license on differing terms that have not been
  established, and the wrong claim would be worse than none.
-->
<#list sourceGraphs![] as source>
<${source.graph()}>
    a void:Dataset ;
    dcterms:title "${lit(source.title())}"@en ;
    dcterms:description "${lit(source.description())}"@en ;
    void:sparqlEndpoint <${sparqlUrl}> ;
    void:uriSpace "${source.graph()}" ;
<#list source.vocabularies() as vocabulary>
    void:vocabulary <${vocabulary}> ;
</#list>
<#if source.licence()??>
    dcterms:license <${source.licence()}> ;
</#if>
    .
</#list>
<#list catalogues as cat>

<${baseUri}/${cat.id}/documents>
    a void:Dataset ;
    dcterms:title "${lit(cat.title)}"@en ;
    dcterms:subject <http://dbpedia.org/resource/Environmental_science> ;
    foaf:homepage <${baseUri}/${cat.id}/documents> ;
    void:sparqlEndpoint <${sparqlUrl}> ;
    void:dataDump <${baseUri}/${cat.id}/catalogue.ttl> ;
    void:vocabulary <http://www.w3.org/ns/dcat#> ;
    void:vocabulary <http://purl.org/dc/terms/> ;
    void:vocabulary <http://www.opengis.net/ont/geosparql#> ;
    void:vocabulary <http://www.w3.org/ns/prov#> ;
    void:vocabulary <http://xmlns.com/foaf/0.1/> ;
<#if (stats[cat.id])??>
    void:entities ${stats[cat.id].entities()?c} ;
    void:triples ${stats[cat.id].triples()?c} ;
<#assign classCounts = stats[cat.id].classEntityCounts()>
<#list classCounts?keys as classUri>
    void:classPartition [ void:class <${classUri}> ; void:entities ${classCounts[classUri]?c} ] ;
</#list>
</#if>
    void:propertyPartition [ void:property dcterms:title ] ;
    void:propertyPartition [ void:property dcterms:description ] ;
    void:propertyPartition [ void:property dcterms:identifier ] ;
    void:propertyPartition [ void:property dcterms:subject ] ;
    void:propertyPartition [ void:property dcterms:spatial ] ;
    void:propertyPartition [ void:property dcterms:temporal ] ;
    void:propertyPartition [ void:property dcat:contactPoint ] ;
    void:propertyPartition [ void:property dcterms:publisher ] ;
    void:propertyPartition [ void:property dcterms:creator ] ;
    void:propertyPartition [ void:property dcterms:license ] ;
    void:propertyPartition [ void:property dcterms:rights ] ;
    void:propertyPartition [ void:property dcat:distribution ] ;
    void:propertyPartition [ void:property prov:wasGeneratedBy ] ;
    .
</#list>
