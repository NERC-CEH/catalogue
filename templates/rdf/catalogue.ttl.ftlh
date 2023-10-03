@prefix dcat: <http://www.w3.org/ns/dcat#> .
@prefix dct: <http://purl.org/dc/terms/> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix vcard: <http://www.w3.org/2006/vcard/ns#> .
@prefix foaf: <http://xmlns.com/foaf/0.1/> .
@prefix ${catalogue}: <https://catalogue.ceh.ac.uk/id/> .

<https://catalogue.ceh.ac.uk/${catalogue}/documents/> a dcat:Catalog ;
    dct:title "${catalogue} digital asset register"@en ;
    foaf:homepage <https://catalogue.ceh.ac.uk/${catalogue}/documents> ;
    dcat:record <#list records as record>${catalogue}:${record} <#sep>,</#sep></#list> ;
.

<#if catalogue=="eidc" >
<https://ror.org/04xw4m193> a vcard:Organization ;
    vcard:fn "NERC EDS Environmental Information Data Centre" ;
.
<https://ror.org/00pggkr55> a vcard:Organization ;
    vcard:fn "UK Centre for Ecology & Hydrology" ;
.
</#if>