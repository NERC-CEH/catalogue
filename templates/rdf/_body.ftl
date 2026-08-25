:${id}
  dcterms:title <@displayLiteral title /> ;

  <@canonicalIdentifier resourceIdentifiers />
  <@otherIdentifiers resourceIdentifiers />

  <#if datacitable?string=='true' && citation?has_content>
      <#assign citationString =  citation.authors?join(', ') + " (" + citation.year?string("0") +"). " + citation.title + ". " + citation.publisher + ". " + citation.url?trim>
      dcterms:bibliographicCitation <@displayLiteral citationString/> ;
  </#if>

  <#if availability != "Deleted">
    <#if description?has_content>
      dcterms:description <@displayLiteral description /> ;
    </#if>

    <#if lineage?has_content>
      dcterms:provenance [
        a dcterms:ProvenanceStatement ;
        rdfs:label <@displayLiteral lineage />
      ] ;
    </#if>

    <#list boundingBoxes as extent>
     dcterms:spatial [
        a dcterms:Location ;
        dcat:bbox "${extent.wkt}"^^geo:wktLiteral ;
      ] ;
    </#list>

    <#if temporalExtents?has_content>
      <@temporal />
    </#if>

    <#--Points of contact-->
    <#if contactPoints?has_content>
      dcat:contactPoint <@contactList contactPoints "c" />  ;
    </#if>

    <#--Publisher-->
    <#if publishers?has_content>
      dcterms:publisher <@contactList publishers "pub" /> ;
    </#if>

    <#--Relationships-->
    <#list jena.relationships(uri, "http://purl.org/dc/terms/isPartOf")>
      dcterms:isPartOf <#items as item><${item.href}><#sep>, </#items> ;
    </#list>
    <#list jena.relationships(uri, "http://purl.org/dc/terms/replaces")>
      dcterms:replaces <#items as item><${item.href}><#sep>, </#items> ;
    </#list>

    <#list jena.relationships(uri, "http://purl.org/dc/terms/relation")>
      dcterms:relation <#items as item><${item.href}><#sep>, </#items> ;
    </#list>

    <#--Citations-->
    <#if incomingCitations?has_content>
      dcterms:isReferencedBy <@incomingCitationList /> ;
    </#if>

    <#if allKeywords?has_content>
      dcterms:subject <@keywordList allKeywords/> ;
    </#if>

    <#if fileset?? && fileset?has_content && fileset?filter(fs -> fs.observedProperty?has_content)?has_content>
      sdo:variableMeasured <@opList /> ;
    </#if>

    <#if fileset?? && fileset?has_content && fileset?filter(fs -> fs.observedProperty?filter(op -> observedPropertyUri(op)?has_content)?has_content)?has_content>
      sosa:observedProperty <@opSosaList /> ;
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
    <#elseif type=='software' || type=='model'|| type=='computationalNotebook'>
      <#include "turtle/_code.ftl">
    </#if>

    dcterms:language <http://id.loc.gov/vocabulary/iso639-1/en> . <#-- leave here to close all the statements about the dataset -->

    <#if contactPoints?has_content>
      <@contactDetail contactPoints "c" />
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

    <#if allKeywords?has_content>
      <@keywordDetail allKeywords />
    </#if>

    <#if fileset?? && fileset?has_content>
      <@opDetail />
    </#if>

    <#if authorPointOfContactWithRORs?has_content>
      <@organisationRORs />
    </#if>

    <@fundingDetail />
  <#else>
    dcterms:description "This resource is no longer available please contact the Environmental Information Data Centre for more details" ;
    .
  </#if>

  <#macro canonicalIdentifier resourceIdentifiers>

    <#local domain = uri?replace(id, "")>
    <#local canonicalId = resourceIdentifiers?filter(id -> id.code?starts_with(domain))?first!>

    <#if canonicalId?has_content>
      dcterms:identifier <${canonicalId.code}> ;
    </#if>

  </#macro>

  <#macro otherIdentifiers resourceIdentifiers>

    <#local domain = uri?replace(id, "")>
    <#local dois = resourceIdentifiers?filter(id -> id.codeSpace?matches("doi"))!>
    <#local otherIds = resourceIdentifiers?filter(id -> !id.code?starts_with(domain) && !(id.codeSpace?? && id.codeSpace?matches("doi")) )>

    <#if dois?has_content>

      <#list dois>
        adms:identifier <#t>
          <#items as id>
            <https://doi.org/${id.code}><#t>
          <#sep>,</#sep><#t>
          </#items> ;<#t>
      </#list>

    </#if>

    <#if otherIds?has_content>

      <#list otherIds>
        adms:identifier <#t>
          <#items as id>
            "<#if id.codeSpace?? && id.codeSpace?has_content && !id.codeSpace?starts_with("http")>${id.codeSpace}/</#if>${id.code}"<#t>
          <#sep>,</#sep><#t>
          </#items> ;<#t>
      </#list>

    </#if>

  </#macro>


  <#macro idSameAs resourceIdentifiers>

    <#local domain = uri?replace(id, "")>
    <#local otherIds = resourceIdentifiers?filter(id -> !id.code?starts_with(domain) && !(id.codeSpace?? && id.codeSpace?matches("doi")) )>

    <#if otherIds?has_content>

      <#list otherIds>
        adms:identifier <#t>
          <#items as id>
            "<#if id.codeSpace?? && id.codeSpace?has_content && !id.codeSpace?starts_with("http")>${id.codeSpace}/</#if>${id.code}"<#t>
          <#sep>,</#sep><#t>
          </#items> ;<#t>
      </#list>

    </#if>

  </#macro>
