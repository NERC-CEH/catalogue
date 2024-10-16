:${id}
  dct:title "<@displayLiteral title />" ;

  <@identifiers resourceIdentifiers />

  <#if datacitable?string=='true' && citation?has_content>
      dct:bibliographicCitation "${citation.authors?join(', ')} (${citation.year?string("0")}). <@displayLiteral citation.title />. ${citation.publisher}. ${citation.url?trim}" ;
  </#if>

  <#if resourceStatus != "Deleted">
    <#if description?has_content>
      dct:description "<@displayLiteral description />" ;
    </#if>

    <#if lineage?has_content>
      dct:provenance [
        a dct:ProvenanceStatement ;
        rdfs:label "<@displayLiteral lineage />"
      ] ;
    </#if>

    <#list boundingBoxes as extent>
      dct:spatial "${extent.wkt}"^^geo:wktLiteral ;
    </#list>

    <#if temporalExtents?has_content>
      <#list temporalExtents as extent>
        dct:temporal "${(extent.begin?date)!''}/${(extent.end?date)!''}"^^dct:PeriodOfTime ;
      </#list>
    </#if>

    <#--Points of contact-->
    <#if pointsOfContact?has_content>
      dcat:contactPoint <@contactList pointsOfContact "c" />  ;
    </#if>

    <#--Publisher-->
    <#if publishers?has_content>
      dct:publisher <@contactList publishers "pub" /> ;
    </#if>

    <#--Relationships-->
    <#list jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#memberOf")>
      dct:isPartOf <#items as item><${item.href}><#sep>, </#items> ;
    </#list>
    <#list jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#supersedes")>
      dct:replaces <#items as item><${item.href}><#sep>, </#items> ;
    </#list>
    <#list jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#relatedTo")>
      dct:relation <#items as item><${item.href}><#sep>, </#items> ;
    </#list>

    <#--Citations-->
    <#--
    <#if incomingCitations?has_content>
      dct:isReferencedBy <#t>
      <#list incomingCitations as citation>
        <${citation.url?trim}><#sep>,</#sep>
      </#list>
      ;
    </#if>
    -->

    <#if incomingCitations?has_content>
      dct:isReferencedBy <@incomingCitationList /> ;
    </#if>

    <#if allKeywords?has_content>
    dct:subject <@keywordList allKeywords/> ;
    </#if>

    <#if funding?has_content>
    prov:wasGeneratedBy <@fundingList /> ;
    </#if>

    <#if type=='dataset' || type=='nonGeographicDataset' || type=='signpost'>
      <#include "turtle/_dataset.ftl">
    <#elseif type=='aggregate'|| type=='collection'|| type=='series'>
      <#include "turtle/_aggregation.ftl">
    <#elseif type=='service'>
      <#include "turtle/_service.ftl">
    <#elseif type=='application'>
      <#include "turtle/_application.ftl">
    </#if>

    dct:language "eng" . <#-- leave here to close all the statements about the dataset -->

    <#if pointsOfContact?has_content>
      <@contactDetail pointsOfContact "c" />
    </#if>

    <#if publishers?has_content>
      <@contactDetail publishers "pub" />
    </#if>

    <#if authors?has_content>
      <@contactDetail authors "a" />
    </#if>

    <#if incomingCitations?has_content>
      <@incomingCitationDetail />
    </#if>

    <@keywordDetail allKeywords/>
    <@fundingDetail />
  <#else>
    dct:description "This resource is no longer available please contact the Environmental Information Data Centre for more details" ;
    .
  </#if>

  <#macro identifiers resourceIdentifiers>
    <#list resourceIdentifiers >
    dct:identifier <#t>
      <#items as id>
        "<#t>
        <#if id.codeSpace?starts_with("doi")>
          https://doi.org/<#t>
        </#if>
          ${id.code}"<#t>
      <#sep>,</#sep><#t>
      </#items> ;<#t>
    </#list>
  </#macro>
