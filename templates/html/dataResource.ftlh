<#import "blocks.ftlh" as blocks>
<#import "skeleton.ftlh" as skeleton>
<#import "../functions.ftlh" as func>

<#assign
    otherContacts = publishers + rightsHolders
    catalogue = catalogues.retrieve(metadata.catalogue)

    rel_all=jena.allRelatedRecords(uri)
    rel_related=jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#relatedTo") + jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#relatedTo")
    rel_generates = jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#generates")
    rel_uses = jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#uses")
    rel_partOf = jena.relationships(uri, "http://purl.org/dc/terms/isPartOf")
    rel_generatedBy= jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#generates")
    rel_supersededBy = jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#supersedes")
    rel_usedBy = jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#uses")
    rel_hasPart = jena.inverseRelationships(uri, "http://purl.org/dc/terms/isPartOf")
    rel_supersedes = jena.supersedes(uri)
    rel_source = jena.relationships(uri, "http://purl.org/dc/terms/source")

    MD_checks = metadataQuality.check(id)
    problems = MD_checks.getProblems()
    errors = MD_checks.getErrors()
    infos =  MD_checks.getInfo()
    warnings = MD_checks.getWarnings()
    errorList = MD_checks.getErrorList()
    infoList = MD_checks.getInfoList()
    warningList = MD_checks.getWarningList()
    totalIssues = errors + infos +  warnings
    metadataClass="warning"
>
<#assign apps=browsingApps>

<#if totalIssues?? && totalIssues gte 1>
  <#if errors gte 1 >
      <#assign metadataClass="danger">
  <#elseif warnings gte 1 >
      <#assign metadataClass="warning" >
  <#elseif infos gte 1 >
      <#assign metadataClass="primary">
  </#if>
</#if>

<#assign isLinkedDoc=false linkedClass="" >
<#if rel_source?has_content && rel_source?size gt 0>
  <#assign isLinkedDoc=true linkedClass="linked" >
</#if>

<#macro getLabel val array>
  <#list array as item>
    <#if item['value']==val>
      ${item['label']}
      <#break/>
    </#if>
  </#list>
</#macro>

<@skeleton.master title=title catalogue=catalogue rdf="${uri}?format=ttl" schemaorg="${uri}?format=schema.org" canonical="${uri}" can_edit_restricted=permission.userCanEditRestrictedFields(metadata.catalogue) footer=true>
  <div id="metadata" class="metadata ${metadata.state?lower_case} ${linkedClass}">
    <div class="container">
      <#if resourceType?has_content && resourceType.value !=''>
        <#assign recordType = func.recordTypeOrResourceType()>

          <#include "dataResource/_admin.ftlh">

          <#if availability = "Deleted">
            <#include "dataResource/_typeTombstone.ftlh">
          <#else>
              <#if resourceType.value == 'aggregate' || resourceType.value == 'collection'>
                <#include "dataResource/_typeAggregate.ftlh">
              <#else>
                <#include "dataResource/_typeDataset.ftlh">
              </#if>

              <#if permission.userCanEditRestrictedFields(metadata.catalogue) && metadata.catalogue == "eidc">
                <#include "dataResource/_metadataqualityReport.ftlh">
              </#if>
          </#if>

      <#else>
        <div class="alert alert-danger missingResourceType" id="metadata">
          <p class="clearfix"><i class="fa-solid fa-triangle-exclamation"></i> <b>WARNING: </b> resource type is missing from this record
          <a class="edit-control" data-document-type="${metadata.documentType}">Edit</a></p>
        </div>
      </#if>

      <script type="application/ld+json">
        <#include "../schema.org/schema.org.ftl">
      </script>
    </div>
  </div>
</@skeleton.master>
