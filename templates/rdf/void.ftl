@prefix void:    <http://rdfs.org/ns/void#> .
@prefix foaf:    <http://xmlns.com/foaf/0.1/> .
@prefix dcterms: <http://purl.org/dc/terms/> .
@prefix dcat:    <http://www.w3.org/ns/dcat#> .
@prefix dcmitype: <http://purl.org/dc/dcmitype/> .
@prefix doo:     <https://digital.ceh.ac.uk/ontology/doo/> .
@prefix prov:    <http://www.w3.org/ns/prov#> .

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
