<#import "blocks.ftlh" as blocks>
<#import "skeleton.ftlh" as skeleton>
<#import "../functions.ftlh" as func>

<#assign
    authors = func.filter(responsibleParties, "role", "author")
    pocs = func.filter(responsibleParties, "role", "pointOfContact")
    custodians = func.filter(responsibleParties, "role", "custodian")
    publishers = func.filter(responsibleParties, "role", "publisher")
    depositors = func.filter(responsibleParties, "role", "depositor")
    originators = func.filter(responsibleParties, "role", "originator")
    rightsHolders = func.filter(responsibleParties, "role", "rightsHolder")
    owners = func.filter(responsibleParties, "role", "owner")
    originators = func.filter(responsibleParties, "role", "originator")
    resourceProviders = func.filter(responsibleParties, "role", "resourceProvider")
    otherContacts = custodians + publishers + depositors + originators + owners + resourceProviders
    catalogue = catalogues.retrieve(metadata.catalogue)

    rel_all=jena.allRelatedRecords(uri)
    rel_related=jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#relatedTo") + jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#relatedTo")
    rel_generates = jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#generates")
    rel_uses = jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#uses")
    rel_memberOf = jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#memberOf")
    rel_generatedBy= jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#generates")
    rel_supersededBy = jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#supersedes")
    rel_usedBy = jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#uses")
    rel_hasMember = jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#memberOf")
    rel_supersedes = jena.supersedes(uri)
    rel_source = jena.relationships(uri, "http://purl.org/dc/terms/source")
>
<#if useConstraints?has_content>
  <#assign
    licences = func.filter(useConstraints, "code", "license")
    otherConstraints =  func.filter(useConstraints, "code", "license", true)
  >
</#if>

<#assign isLinkedDoc=false linkedClass="" >
<#if rel_source?has_content && rel_source?size gt 0>
  <#assign isLinkedDoc=true linkedClass="linked" >
</#if>

<#if isLinkedDoc && resourceIdentifiers?has_content>
  <#assign masterDocument = func.filterRegex(resourceIdentifiers, "code", "https://catalogue.ceh.ac.uk/id/")>
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
  <div id="metadata" class="mt-3 metadata ${metadata.state?lower_case} ${linkedClass}">
    <div class="container">
      <#if resourceType?has_content && resourceType.value !=''>
        <#assign recordType = func.recordTypeOrResourceType()>

          <#include "dataResource/_admin.ftlh">

          <#if resourceStatus = "Deleted">
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
        <#include "../schema.org/schema.org.ftlh">
      </script>
    </div>
  </div>
</@skeleton.master>
