<#compress>
<#include "_prefixes.ftlh">
@prefix : <${baseUri}/id/> .
@base <${baseUri}/> .

<${baseUri}/${catalogue}/documents/> a dcat:Catalog ;
    dct:title "${title} digital asset register"@en ;
    foaf:homepage <https://catalogue.ceh.ac.uk/${catalogue}/documents> ;
    <#list records>dcat:resource <#items as record>:${record}<#sep>, </#items>;</#list>
.

<#if catalogue=="eidc" >
<https://ror.org/04xw4m193> a vcard:Organization ;
    vcard:fn "NERC EDS Environmental Information Data Centre" ;
.
<https://ror.org/00pggkr55> a vcard:Organization ;
    vcard:fn "UK Centre for Ecology & Hydrology" ;
.
</#if>
</#compress>
