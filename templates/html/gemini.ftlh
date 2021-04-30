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

  <div id="metadata" class="${metadata.state?lower_case}">
    <div class="container">
      <#if resourceType?has_content && resourceType.value !=''>
      <#assign recordType = codes.lookup('metadata.recordType',resourceType.value)?lower_case>
      <#include "gemini/_admin.ftlh">
      <div id="section-Top">
        <#include "gemini/_title.ftlh">

      </div>

      <#if rel_supersedes?has_content && rel_supersedes?size gt 0>
        <#include "gemini/_latestVersion.ftlh">
      </#if>

      <#if permission.userCanEditRestrictedFields(metadata.catalogue) && metadata.catalogue == "eidc">
        <#include "gemini/_metadataqualityAlert.ftlh">
      </#if>  

        <div id="document-description" class="clearfix">
          <#include "gemini/_browseGraphic.ftlh">
          <@blocks.linebreaks description!"" />
        </div>

        <#if resourceType.value != 'aggregate' && resourceType.value != 'collection'>
          <#include "gemini/_dates.ftlh">
                    
          <div class="row">
            <div class="col-sm-4 col-sm-push-8 panel-right">
              <#include "gemini/_uploadData.ftlh">
              <#include "gemini/_metrics.ftlh">

              <#if resourceStatus??>
                <#if resourceStatus == 'Restricted'>
                  <#include "gemini/_distribution_restricted.ftlh">
                <#else>
                  <#if resourceType.value == 'signpost'>
                    <#include "gemini/_distribution_signpost.ftlh">
                  <#elseif resourceType.value == 'service' && mapViewable>
                    <#include "gemini/_distribution_service.ftlh">
                  <#elseif resourceType.value == 'dataset' || resourceType.value == 'nonGeographicDataset' || resourceType.value == 'application'>
                    <#include "gemini/_distribution_dataset.ftlh">
                  </#if>
                </#if>
              <#elseif metadata.catalogue != "eidc" &&  resourceType.value == 'signpost'>
                 <#include "gemini/_distribution_signpost.ftlh">
              </#if>

              <#if resourceType.value == 'thirdPartyDataset'>
                <#include "gemini/_third_party.ftlh">
              </#if>

            </div>
            <div class="col-sm-8 col-sm-pull-4">
              <#include "gemini/_extent.ftlh">
              <#include "gemini/_dataquality.ftlh">

              <#-- to be replaced with related records -->
                <#include "gemini/_related.ftlh">
              <#-- END OF to be replaced -->

              <#include "gemini/_supplemental.ftlh">
              <#include "gemini/_contacts.ftlh">

              <#if topicCategories?? || descriptiveKeywords?? ||  inspireTheme?? || funding?? || spatialRepresentationTypes?? || spatialReferenceSystems??>
                <h3>Additional metadata</h3>
                <dl id="otherInfo" class="dl-horizontal">
                  <#include "gemini/_tags.ftlh">
                  <#include "gemini/_funding.ftlh">
                  <#if resourceType?has_content && resourceType.value !='nonGeographicDataset'>
                    <#include "gemini/_spatial.ftlh">
                  </#if>
                   <#if metadataDateTime?has_content>
                    <dt>Last updated</dt><dd>${metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']}</dd>
                   </#if>
                </dl>
              </#if>

            </div>
          </div>
          
          <#if permission.userCanEditRestrictedFields(metadata.catalogue) && metadata.catalogue == "eidc">
            <#include "gemini/_metadataqualityReport.ftlh">
          </#if>

        <#else>
          <#include "gemini/_aggregate.ftlh">
        </#if>
      
      <#else>
        <div class="alert alert-danger" id="missingResourceType">
          <p class="clearfix"><i class="fas fa-exclamation-triangle"></i> <b>WARNING: </b> resource type is missing from this record
          <a class="edit-control" data-document-type="${metadata.documentType}">Edit</a></p>
        </div>
      </#if>

    </div>
  </div>

  <div id="footer">
	 <#include "gemini/_footer.ftlh">
	</div>

  <script type="application/ld+json">
	  <#include "../schema.org/schema.org.ftlh">
  </script>

</@skeleton.master>