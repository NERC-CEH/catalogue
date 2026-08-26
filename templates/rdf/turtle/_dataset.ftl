a dcat:Dataset;
dcterms:type dcmitype:Dataset ;

<#if datasetReferenceDate?? && datasetReferenceDate.publicationDate?has_content>
  dcterms:available "${datasetReferenceDate.publicationDate}"^^xsd:date ;
</#if>

<#assign citationLandingPage = "">
<#if datacitable?string=="true" && citation?has_content>
  <#assign citationLandingPage = uriNormaliser.normalise(citation.url!"")>
</#if>
 dcat:landingPage <${uri}><#if citationLandingPage?has_content>, <${citationLandingPage}></#if> ;

 <#include "_rights.ftl"> <#--rights at DATASET level-->

<#if keywordsTheme?has_content>
  dcterms:theme <@keywordList keywordsTheme/> ;
</#if>

<#--Distribution-->
<#list downloads?filter(d -> uriNormaliser.normalise(d.url!"")?has_content)>
dcat:distribution [
    dcat:accessURL
    <#items as download>
      <${uriNormaliser.normalise(download.url)}> <#sep>,
    </#items>
    ;
    <#include "_rights.ftl"> <#--rights at DISTRIBUTION level-->
    <#--
      emitsFormats tracks whether any format node was actually referenced, so
      formatDetail only describes nodes something points at. Set here, inside
      the <#list> body, so it can only be true when the distribution block
      rendered AND a format survived the filter — the two conditions that
      together decide whether the predicate appears at all.
    -->
    <#list (distributionFormats![])?filter(f -> formatUris.hasContent(f))>
    <#assign emitsFormats = true>
    dcterms:format
      <#items as format>
      ${formatUris.identify(format)} <#sep>,
      </#items>
      ;
    </#list>
];
</#list>

<#--Authors-->
<#if authors?has_content>
  dcterms:creator <@contactList authors "a" /> ;
</#if>
