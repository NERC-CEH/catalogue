<#ftl output_format="plainText">
<#compress>
<#import "_turtle.ftl" as ttl>
<#include "_prefixes.ftl">
BASE <${baseUri}/>
PREFIX : <${baseUri}/id/>

<${catalogue}/documents>
   a dcat:Catalog ;
  dcterms:title "${ttl.escape(title)} digital asset register"@en ;
  foaf:homepage <https://catalogue.ceh.ac.uk/${catalogue}/documents> ;
  <#assign publishers = {"eidc": "https://ror.org/04xw4m193", "ukceh": "https://ror.org/00pggkr55"}>
  <#if publishers[catalogue]??>
    dcterms:publisher <${publishers[catalogue]}> ;
  </#if>
  dcterms:license <https://spdx.org/licenses/OGL-UK-3.0.ttl> ;
  dcterms:description "${ttl.escape("Metadata records published in the " + title + " catalogue by UK Centre for Ecology & Hydrology, available under the Open Government Licence.")}"@en ;
  <#if exported?has_content>
    dcterms:modified "${exported}"^^xsd:dateTime ;
  </#if>

  <#list records>
    dcat:resource <#items as record>:${record}<#sep>, </#items> ;
  </#list>
  <#list records>
    dcat:record <#items as record>:${record}_record<#sep>, </#items> ;
  </#list>

  . <#-- leave here to close off all the statements about the catalogue -->

<#if catalogue=="eidc" >
<https://ror.org/04xw4m193>
  a foaf:Organization ;
  foaf:name "NERC EDS Environmental Information Data Centre" .

<https://ror.org/00pggkr55>
  a foaf:Organization ;
  foaf:name "UK Centre for Ecology & Hydrology" .
</#if>
</#compress>
