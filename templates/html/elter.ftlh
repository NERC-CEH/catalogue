<#import "blocks.ftlh" as blocks>
<#import "skeleton.ftlh" as skeleton>
<#import "../functions.ftlh" as func>

<#macro getLabel val array>
  <#list array as item>
    <#if item['value']==val>
      ${item['label']}
      <#break/>
    </#if>
  </#list>
</#macro>

<#assign footerContent>
  <#include "document/_footer.ftlh">
</#assign>


<#-- INITIALISE DOCUMENT DATA-->
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
<#-- END DOCUMENT DATA-->


<#-- WHICH SECTIONS TO DISPLAY -->
  <#assign
    displayDataDownload = 0 
    displayProvenance = 0
    displayCorrespondence = 0
    displayAuthors = 0
    displayAdditional = 0
    displayMetadataQuality = 0
  >

  <#if topicCategories?? || descriptiveKeywords?? ||  inspireTheme?? || funding?? || spatialRepresentationTypes?? || spatialReferenceSystems??>
     <#assign displayAdditional = 1 >
  </#if>
  <#if pocs?size gt 0 >
     <#assign displayCorrespondence = 1 >
  </#if>
  <#if authors?size gt 0 || otherContacts?has_content || rightsHolders?has_content>
     <#assign displayAuthors = 1 >
  </#if>
   
  <#if permission.userCanEditRestrictedFields(metadata.catalogue) && metadata.catalogue == "eidc">
     <#assign displayMetadataQuality = 1 >
  </#if>
<#-- END SECTIONS -->



<@skeleton.master title=title catalogue=catalogue rdf="${uri}?format=ttl" schemaorg="${uri}?format=schema.org" canonical="${uri}" can_edit_restricted=permission.userCanEditRestrictedFields(metadata.catalogue) footer=footerContent>
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

                  <#include "document/_elterSpecific.ftlh">
                  <#include "document/_formats.ftlh">
                  <#include "document/_dataquality.ftlh">
                  <#include "document/_extent.ftlh">
                  <a name="related"></a><#include "document/_related.ftlh">
                  <a name="citations"></a><#include "document/_citation_list.ftlh">
                  <a name="supplemental"></a><#include "document/_supplemental.ftlh">
                  <#include "document/_contacts.ftlh">

                  <#if displayAdditional = 1>
                    <a name="additional"></a>
                    <h2>Additional metadata</h2>
                    <dl id="otherInfo" class="dl-horizontal">
                      <#include "document/_tags.ftlh">
                      <#include "document/_funding.ftlh">
                      <#if resourceType?has_content && resourceType.value !='nonGeographicDataset'>
                        <#include "document/_spatial.ftlh">
                      </#if>
                        <#if metadataDateTime?has_content>
                        <dt>Last updated</dt><dd>${metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']}</dd>
                        </#if>
                    </dl>
                  </#if>
                </div>
                <div class="col2">
                  <#if isLinkedDoc!="true">
                    <#include "document/_metadataMenu.ftlh">
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

  <script type="application/ld+json">
	  <#include "../schema.org/schema.org.ftlh">
  </script>

</@skeleton.master>
