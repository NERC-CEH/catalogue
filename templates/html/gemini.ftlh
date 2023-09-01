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
    otherContacts = custodians + publishers + depositors + originators + owners + resourceProviders
    catalogue = catalogues.retrieve(metadata.catalogue)
    isLinkedDoc="false"
    linkedClass=""

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

<#if rel_source?has_content && rel_source?size gt 0>
  <#assign isLinkedDoc="true" linkedClass="linked" >
</#if>

<#if isLinkedDoc="true" && resourceIdentifiers?has_content>
  <#assign masterDocument = func.filterRegex(resourceIdentifiers, "code", "https://catalogue.ceh.ac.uk/id/")>
</#if>
<#-- END DOCUMENT DATA-->

<#-- METADATA QUALITY INFO-->
  <#assign
      MD_checks = metadataQuality.check(id)
      problems = MD_checks.getProblems()
      errors = func.filter(problems, "severity", "ERROR")
      warnings = func.filter(problems, "severity", "WARNING")
      infos = func.filter(problems, "severity", "INFO")
      problemCount=0
      alertclass=""
      alerticon=""
  >
  <#if infos?size gte 1 ><#assign alertclass="info" alerticon="fa-circle-info" problemCount=problemCount+infos?size></#if>
  <#if warnings?size gte 1 ><#assign alertclass="warning" alerticon="fa-circle-exclamation" problemCount=problemCount+warnings?size></#if>
  <#if errors?size gte 1 ><#assign alertclass="error" alerticon="fa-exclamation-triangle" problemCount=problemCount+errors?size></#if>
<#-- END METADATA QUALITY -->


<#-- WHICH SECTIONS TO DISPLAY -->
  <#assign
    displayDataDownload = 0 
    displayProvenance = 0
    displayCorrespondence = 0
    displayAuthors = 0
    displayAdditional = 0
    displayMetadataQuality = 0
    displayCitations = 0
    displaySupplemental = 0
  >

  <#if topicCategories?? || descriptiveKeywords?? ||  inspireTheme?? || funding?? || spatialRepresentationTypes?? || spatialReferenceSystems?? ||boundingBoxes?? || temporalExtents?? || geometries?? >
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
                  <li><a href="${uri}/datacite.xml">Datacite xml</a></li>
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

              <#if isLinkedDoc!="true">
                  <#include "document/_uploadData.ftlh">
                  <#--include "document/_metrics.ftlh"-->
                 
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

              <#include "document/_formats.ftlh">
              <#include "document/_dataquality.ftlh">
              <#include "document/_related.ftlh">
              <#include "document/_citation_list.ftlh">
              <#include "document/_supplemental.ftlh">
              <#include "document/_contacts.ftlh">
              
              <#if displayAdditional = 1>
                <div>
                  <h2>Additional metadata  <a name="additional"></a></h2>
                  <dl id="otherInfo" class="dl-horizontal">
                    <#include "document/_tags.ftlh">
                    <#include "document/_extent.ftlh">
                    <#include "document/_funding.ftlh">
                    <#if resourceType?has_content && resourceType.value !='nonGeographicDataset'>
                      <#include "document/_spatial.ftlh">
                    </#if>
                      <#if metadataDateTime?has_content>
                      <dt>Metadata last updated</dt><dd>${metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']}</dd>
                      </#if>
                  </dl>
                </div>
              </#if>

               <#if displayMetadataQuality = 1>
                  <#include "document/_metadataqualityReport.ftlh">
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
      <a class="edit-control" data-document-type="${metadata.documentType}">Edit</a></p>
    </div>
  </#if>
  
  <script type="application/ld+json">
	  <#include "../schema.org/schema.org.ftlh">
  </script>

</@skeleton.master>
