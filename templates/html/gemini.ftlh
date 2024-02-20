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

<#assign isLinkedDoc="false" linkedClass="" >
<#if rel_source?has_content && rel_source?size gt 0>
  <#assign isLinkedDoc="true" linkedClass="linked" >
</#if>

<#if isLinkedDoc="true" && resourceIdentifiers?has_content>
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

<@skeleton.master title=title catalogue=catalogue rdf="${uri}?format=ttl" schemaorg="${uri}?format=schema.org" canonical="${uri}" can_edit_restricted=permission.userCanEditRestrictedFields(metadata.catalogue)>
  <#if resourceType?has_content && resourceType.value !=''>
  <#assign recordType = func.recordTypeOrResourceType()>

  <div id="metadata" class="${metadata.state?lower_case} ${linkedClass}">

    <div class="section section-grey">
      <div class="container">
        <#include "document/_admin.ftlh">
        <#if title?? && title?has_content>
           <#include "document/_title.ftlh">
        </#if>

        <#if rel_supersedes?has_content && rel_supersedes?size gt 0>
            <#include "document/_latestVersion.ftlh">
        </#if>

        <#if resourceType.value=="application" && metadata.catalogue == "eidc">
          <div class="disclaimer">
            <strong>DISCLAIMER:</strong> Publication of this ${recordType?lower_case!''} by the EIDC does not signify any endorsement or approval. By accessing and using the resource, you acknowledge that it is entirely at your own risk and you are solely responsible for any loss or liability that may arise.
          </div>
        </#if>

        <#if description??>
          <div class="description">
            <div class="description-text">
              <@blocks.linebreaksAndLinks description!"" />

              <#if resourceType.value != 'aggregate' && resourceType.value != 'collection'>
                <#if datasetReferenceDate??>
                  <#if datasetReferenceDate.publicationDate??>
                    <div class="publicationDate">
                      Publication date: ${datasetReferenceDate.publicationDate}
                    </div>
                  </#if>
                </#if>
              </#if>

            </div>
            <div class="browseimage">
              <#include "document/_browseGraphic.ftlh">
            </div>
          </div>
        </#if>

        <#if resourceType.value != 'aggregate' && resourceType.value != 'collection'>
          <div class="pull-right btn-group alternativeViews">
            <span aria-expanded="false" aria-haspopup="true" data-toggle="dropdown" class="dropdown-toggle " type="button">
                View record as <span class="caret"></span>
            </span>
            <ul class="dropdown-menu dropdown-menu-right">
              <li><a href="${uri}?format=json">json</a></li>
              <#if resourceType?? && (resourceType.value == 'dataset' || resourceType.value == 'application' || resourceType.value == 'service' )>
                <#if citation??>
                  <li><a href="${uri?replace("/id/", "/documents/")}/datacite.xml">Datacite xml</a></li>
                </#if>
                <li><a href="${uri}.xml?format=gemini">ISO 19115 (XML)</a></li>
                <li><a href="${uri}?format=schema.org">schema.org (json-LD)</a></li>
                <li><a href="${uri}?format=ttl">RDF (Turtle)</a></li>
              </#if>
            </ul>
          </div>
        </#if>

      </div>
    </div>

    <#if permission.userCanEditRestrictedFields(metadata.catalogue) && metadata.catalogue == "eidc">
      <div class="section"><div class="container">
        <#include "document/_metadataqualityAlert.ftlh">
      </div></div>
    </#if>

    <#if resourceType.value == 'aggregate' || resourceType.value == 'collection'>
      <div class="section">
        <div class="container">
          <#include "document/_aggregate.ftlh">
        </div>
      </div>
    <#else>
      <#if isLinkedDoc="true">
       <div class="section">
        <div class="container">
          <#include "document/_linked.ftlh">
         </div>
        </div>
      </#if>
      <div class="section">
        <div class="container">
          <div class="metadata-grid">
            <div class="col1">
              <#include "document/_formats.ftlh">
              <#if resourceType?has_content && resourceType.value !='nonGeographicDataset'><#include "document/_spatial.ftlh"></#if>
              <#include "document/_temporal.ftlh">
              <#include "document/_dataquality.ftlh">
              <#include "document/_related.ftlh">
              <#include "document/_citation_list.ftlh">
              <#include "document/_supplemental.ftlh">
              <#include "document/_contacts.ftlh">

              <#if topicCategories?? || descriptiveKeywords?? || inspireTheme?? || funding?? >
                <h2>Additional metadata</h2>
                <dl id="otherInfo" class="dl-horizontal">
                  <#include "document/_tags.ftlh">
                  <#include "document/_funding.ftlh">
                  <#if metadataDateTime?has_content>
                    <dt>Last updated</dt><dd>${metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']}</dd>
                  </#if>
                </dl>
              </#if>

            </div>
            <div class="col2">

              <#if isLinkedDoc!="true">
                  <#include "document/_uploadData.ftlh">
                  <#include "document/_metrics.ftlh">

                  <#if resourceStatus?? && resourceStatus != "Unknown">
                    <#if resourceType.value == 'dataset' || resourceType.value == 'nonGeographicDataset' || resourceType.value == 'application'>
                      <#include "document/_distribution_dataset.ftlh">
                    <#elseif resourceType.value == 'service' && mapViewable>
                      <#include "document/_distribution_service.ftlh">
                    <#elseif resourceType.value == 'nercSignpost' || resourceType.value == 'signpost'>
                      <#include "document/_distribution_signpost.ftlh">
                    </#if>
                  </#if>

                  <#if resourceType.value == 'thirdPartyDataset'>
                    <#include "document/_third_party.ftlh">
                  </#if>
              </#if>

          </div>
        </div>
      </div>
    </#if>

    <#if permission.userCanEditRestrictedFields(metadata.catalogue) && metadata.catalogue == "eidc">
        <div class="section">
            <div class="container">
                <#include "document/_metadataqualityReport.ftlh">
            </div>
        </div>
    </#if>

  </div>

  <#else>
    <div class="alert alert-danger missingResourceType" id="metadata">
      <p class="clearfix"><i class="fa-solid fa-triangle-exclamation"></i> <b>WARNING: </b> resource type is missing from this record
      <a class="edit-control" data-document-type="${metadata.documentType}">Edit</a></p>
    </div>
  </#if>

  <#include "document/_footer.ftlh">

  <script type="application/ld+json">
	  <#include "../schema.org/schema.org.ftlh">
  </script>

</@skeleton.master>
