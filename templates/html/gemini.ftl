<#import "blocks.ftl" as blocks>
<#import "skeleton.ftl" as skeleton>
<#import "../functions.tpl" as func>

<#assign
    authors = func.filter(responsibleParties, "role", "author")
    pocs = func.filter(responsibleParties, "role", "pointOfContact")
    custodians = func.filter(responsibleParties, "role", "custodian")
    publishers = func.filter(responsibleParties, "role", "publisher")
    depositors = func.filter(responsibleParties, "role", "depositor")
    originators = func.filter(responsibleParties, "role", "originator")
    owners = func.filter(responsibleParties, "role", "owner")
    originators = func.filter(responsibleParties, "role", "originator")
    resourceProviders = func.filter(responsibleParties, "role", "resourceProvider")
    otherContacts = custodians + publishers + depositors + originators + owners + resourceProviders
>

<#macro getLabel val array>
  <#list array as item>
    <#if item['value']==val>
      ${item['label']}
      <#break/>
    </#if>
  </#list>
</#macro>
<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue) rdf="${uri}?format=ttl" schemaorg="${uri}?format=schema.org" canonical="${uri}" can_edit_restricted=permission.userCanEditRestrictedFields(metadata.catalogue)>

  <div id="metadata">
    <div class="container">
      <#if resourceType?has_content && resourceType.value !=''>
      <#include "gemini/_admin.ftl">

        <div id="section-Top">
          <#include "gemini/_title.ftl">
        </div>

        <@blocks.description description!"" />
        <#if resourceType.value != 'aggregate' && resourceType.value != 'collection'>
          <#include "gemini/_dates.ftl">
          <div class="row">
            <div class="col-sm-4 col-sm-push-8">
              <#include "gemini/_uploadData.ftl">
              <#include "gemini/_distribution.ftl">
              <#include "gemini/_reuse.ftl">
              <#include "gemini/_children.ftl">
              <#include "gemini/_related.ftl">
              <#include "gemini/_model.ftl">
            </div>
            <div class="col-sm-8 col-sm-pull-4">
              <#include "gemini/_extent.ftl">
              <#include "gemini/_supplemental.ftl">
              <#include "gemini/_dataquality.ftl">
              <#include "gemini/_contacts.ftl">
              <#if resourceType?has_content && resourceType.value !='nonGeographicDataset'>
                <#include "gemini/_spatial.ftl">
              </#if>
              <#include "gemini/_tags.ftl">
            </div>
          </div>
          
        <#else>
          <#include "gemini/_aggregate.ftl">
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
	 <#include "gemini/_footer.ftl">
	</div>

  <script type="application/ld+json">
	  <#include "../schema.org/schema.org.tpl">
  </script>

</@skeleton.master>