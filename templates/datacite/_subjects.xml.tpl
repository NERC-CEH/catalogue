<#if doc.descriptiveKeywords?has_content>
<subjects>
  <#list doc.descriptiveKeywords as descriptiveKeyword>
  <#list descriptiveKeyword.keywords as keyword>
  <#if keyword.value?has_content>
    <#if keyword.uri?has_content>
      <#if keyword.uri?matches("^http(|s)://inspire.ec.europa.eu/\\S+$")>
        <#assign kwScheme="subjectScheme=\"European Union INSPIRE registry\" schemeURI=\"http://inspire.ec.europa.eu/registry/\"">
      <#elseif keyword.uri?matches("^http(|s)://www.wikidata.org/entity/\\S+$")>
        <#assign kwScheme="subjectScheme=\"Wikidata\" schemeURI=\"https://www.wikidata.org/\"">
      <#elseif keyword.uri?matches("^http(|s)://sws.geonames.org/\\S+$")>
        <#assign kwScheme="subjectScheme=\"Geonames\" schemeURI=\"http://www.geonames.org/\"">
      <#else>
        <#assign kwScheme="">
      </#if> 
      <subject valueURI="${keyword.uri}" <#noescape>${kwScheme}</#noescape>>${keyword.value}</subject>
    <#else>
      <subject>${keyword.value}</subject>
    </#if>
  </#if>
  </#list>
  </#list>
</subjects>
</#if>