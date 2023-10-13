<#include "_prefixes.ftlh">
@prefix : <${baseUri}/> .
@base <${baseUri}/> .

<https://catalogue.ceh.ac.uk/${catalogue}/documents/> a dcat:Catalog ;
    dct:title "${title} digital asset register"@en ;
    foaf:homepage <https://catalogue.ceh.ac.uk/${catalogue}/documents> ;
    <#if records?has_content>dcat:resource <#list records as record>:${record} <#sep>,</#sep></#list> ;</#if>
.

<#if catalogue=="eidc" >
<https://ror.org/04xw4m193> a vcard:Organization ;
    vcard:fn "NERC EDS Environmental Information Data Centre" ;
.
<https://ror.org/00pggkr55> a vcard:Organization ;
    vcard:fn "UK Centre for Ecology & Hydrology" ;
.
</#if>
