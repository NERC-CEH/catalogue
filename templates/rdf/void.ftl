@prefix void:    <http://rdfs.org/ns/void#> .
@prefix foaf:    <http://xmlns.com/foaf/0.1/> .
@prefix dcterms: <http://purl.org/dc/terms/> .

<${baseUri}/.well-known/void>
    a void:DatasetDescription ;
    dcterms:title "UKCEH Metadata Catalogue VoID Description";
<#list catalogues as cat>
    foaf:topic <${baseUri}/${cat.id}/documents> ;
</#list>
    .
<#list catalogues as cat>

<${baseUri}/${cat.id}/documents>
    a void:Dataset ;
    dcterms:title "${cat.title}"@en ;
    dcterms:subject <http://dbpedia.org/resource/Environmental_science> ;
    foaf:homepage <${baseUri}/${cat.id}/documents> ;
    void:sparqlEndpoint <${sparqlUrl}/ds/sparql> ;
    void:dataDump <${baseUri}/${cat.id}/catalogue.ttl> ;
    void:vocabulary <http://www.w3.org/ns/dcat#> ;
    void:vocabulary <http://purl.org/dc/terms/> ;
    void:vocabulary <http://www.opengis.net/ont/geosparql#> ;
    void:vocabulary <http://www.w3.org/ns/prov#> ;
    void:vocabulary <http://xmlns.com/foaf/0.1/> ;
    void:vocabulary <http://www.w3.org/2006/vcard/ns#> ;
<#if stats[cat.id]??>
    void:entities ${stats[cat.id].entities()?c} ;
</#if>
    .
</#list>
