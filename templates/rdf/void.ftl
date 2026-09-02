@prefix void:    <http://rdfs.org/ns/void#> .
@prefix foaf:    <http://xmlns.com/foaf/0.1/> .
@prefix dcterms: <http://purl.org/dc/terms/> .
@prefix dcat:    <http://www.w3.org/ns/dcat#> .
@prefix dcmitype: <http://purl.org/dc/dcmitype/> .
@prefix doo:     <https://digital.ceh.ac.uk/ontology/doo/> .
@prefix prov:    <http://www.w3.org/ns/prov#> .
@prefix skos:    <http://www.w3.org/2004/02/skos/core#> .
@prefix sd:      <http://www.w3.org/ns/sparql-service-description#> .

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
  One dataset per authority whose concept labels the catalogue republishes. The
  catalogue asserts nothing of its own in these graphs — they hold the
  authority's own labels, harvested unchanged — which is exactly why they are
  separate graphs rather than merged into the catalogue's.

  No dcterms:license is claimed: the authorities license on differing terms and
  they have not been established, and the wrong claim would be worse than none.
-->
<#list sourceGraphs![] as source>
<${source.graph()}>
    a void:Dataset ;
    dcterms:title "${source.title()?replace('\\', '\\\\')?replace('"', '\\"')}"@en ;
    dcterms:description "Concept labels as published by the authority, republished unchanged."@en ;
    void:sparqlEndpoint <${sparqlUrl}> ;
    void:uriSpace "${source.graph()}" ;
    void:vocabulary <http://www.w3.org/2004/02/skos/core#> ;
    void:propertyPartition [ void:property skos:prefLabel ] ;
    .
</#list>
<#list catalogues as cat>

<${baseUri}/${cat.id}/documents>
    a void:Dataset ;
    dcterms:title "${cat.title?replace('\\', '\\\\')?replace('"', '\\"')}"@en ;
    dcterms:subject <http://dbpedia.org/resource/Environmental_science> ;
    foaf:homepage <${baseUri}/${cat.id}/documents> ;
    void:sparqlEndpoint <${sparqlUrl}> ;
    void:dataDump <${baseUri}/${cat.id}/catalogue.ttl> ;
    void:vocabulary <http://www.w3.org/ns/dcat#> ;
    void:vocabulary <http://purl.org/dc/terms/> ;
    void:vocabulary <http://www.opengis.net/ont/geosparql#> ;
    void:vocabulary <http://www.w3.org/ns/prov#> ;
    void:vocabulary <http://xmlns.com/foaf/0.1/> ;
    void:vocabulary <http://www.w3.org/2006/vcard/ns#> ;
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
