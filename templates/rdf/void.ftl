@prefix void:    <http://rdfs.org/ns/void#> .
@prefix foaf:    <http://xmlns.com/foaf/0.1/> .
@prefix dcterms: <http://purl.org/dc/terms/> .

<${baseUri}/.well-known/void>
    a void:DatasetDescription ;
    dcterms:title "UKCEH Metadata Catalogue VoID Description";
<#list catalogues as cat>
    foaf:topic <${baseUri}/${cat.id}> ;
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
    .
</#list>
