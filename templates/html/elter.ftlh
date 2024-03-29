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
    otherContacts = custodians + publishers + depositors + originators + owners + resourceProviders + rightsHolders
    catalogue = catalogues.retrieve(metadata.catalogue)
    isLinkedDoc = "false"

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
/>
<#if useConstraints?has_content>
  <#assign
    licences = func.filter(useConstraints, "code", "license")
    otherConstraints =  func.filter(useConstraints, "code", "license", true)>
</#if>
<#if supplemental?has_content>
  <#assign
  referencedBy = func.filter(supplemental, "function", "isReferencedBy")
  supplementTo = func.filter(supplemental, "function", "isSupplementTo")
  websites = func.filter(supplemental, "function", "website")
  supOther = func.filter(supplemental, "function", "") + func.filter(supplemental, "function", "relatedArticle") + func.filter(supplemental, "function", "relatedDataset")>
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
  <div id="metadata" class="${metadata.state?lower_case}">

    <div class="section section-grey">
          <div class="container">
            <#include "document/_admin.ftlh">
            <#if title?? && title?has_content>
              <#include "document/_title.ftlh">
            </#if>

            <#if rel_supersedes?has_content && rel_supersedes?size gt 0>
                <#include "document/_latestVersion.ftlh">
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
          </div>
        </div>
        <#if resourceType.value == 'aggregate' || resourceType.value == 'collection'>
          <div class="section">
            <div class="container">
              <#include "document/_aggregate.ftlh">
            </div>
          </div>
        <#else>
          <div class="section">
            <div class="container">
              <div class="metadata-grid">
                <div class="col1">
                  <#include "document/_elterSpecific.ftlh">
                  <#include "document/_formats.ftlh">
                  <#include "document/_dataquality.ftlh">
                  <#if resourceType?has_content && resourceType.value !='nonGeographicDataset'><#include "document/_spatial.ftlh"></#if>
                  <#include "document/_temporal.ftlh">
                  <#include "document/_related.ftlh">
                  <#include "document/_citation_list.ftlh">
                  <#include "document/_supplemental.ftlh">
                  <#include "document/_contacts.ftlh">

                  <#if topicCategories?? || descriptiveKeywords?? || keywords?? ||  inspireTheme?? || funding??>
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

                  <#if isLinkedDoc="true">
                    <#include "document/_linked.ftlh">
                  <#else>
                      <#include "document/_uploadData.ftlh">
                      <#include "document/_metrics.ftlh">


                      <#if resourceStatus?? && resourceStatus != "Unknown">
                        <#if resourceType.value == 'dataset' || resourceType.value == 'nonGeographicDataset' || resourceType.value == 'application'>
                          <#include "document/_distribution_dataset.ftlh">
                        <#elseif resourceType.value == 'service' && mapViewable>
                          <#include "document/_distribution_service.ftlh">
                        <#elseif resourceType.value == 'signpost'>
                          <#include "document/_distribution_signpost.ftlh">
                        </#if>
                      </#if>

                  </#if>

              </div>
            </div>
          </div>
        </#if>

  </div>
<#else>
  <div class="alert alert-danger missingResourceType" id="metadata">
    <p class="clearfix"><i class="fa-solid fa-triangle-exclamation"></i> <b>WARNING: </b> resource type is missing from this record
    <a class="btn btn-default btn-sm edit-control" data-document-type="${metadata.documentType}">Edit</a></p>
  </div>
</#if>

  <div id="footer">
	 <#include "document/_footer.ftlh">
	</div>

  <script type="application/ld+json">
	  <#include "../schema.org/schema.org.ftlh">
  </script>

</@skeleton.master>
