<#import "blocks.ftl" as blocks>
<#import "skeleton.ftl" as skeleton>
<#import "../underscore.tpl" as _>
<#import "../functions.tpl" as func>

<#assign authors       = _.filter(responsibleParties, _.isAuthor) >
<#assign otherContacts = _.reject(responsibleParties, _.isAuthor) >
<#macro getLabel val array>
  <#list array as item>
    <#if item['value']==val>
      ${item['label']}
      <#break/>
    </#if>
  </#list>
</#macro>
<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue) rdf="${uri}?format=ttl" schemaorg="${uri}?format=schema.org" canonical="${uri}" can_edit_restricted=permission.userCanEditRestrictedFields(metadata.catalogue)>
<#-- TESTING -->
  <#include "gemini/__metadataqualityChecks.ftl">
<#-- END OF TESTING -->
  <div id="metadata">
    <div class="container">
      <#if resourceType?has_content && resourceType.value !=''>
      <#include "gemini/_admin.ftl">

        <div id="section-Top">
          <#include "gemini/_title.ftl">
        </div>

        <#if permission.userCanEditRestrictedFields(metadata.catalogue)>
        <#assign problems = func.filter(MD_checks, "result", "fail")>
          <#if problems?size gte 1>
            <div class="alert alert-warning alert-dismissible" role="alert">
              <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
              <i class="fas fa-info-circle"></i> This metadata record has some quality issues that may need addressing.
            </div>
          </#if>
        </#if>    

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
              <#include "gemini/_authors.ftl">
              <#include "gemini/_otherContacts.ftl">
              <#include "gemini/_spatial.ftl">
              <#include "gemini/_tags.ftl">
            </div>
          </div>
          
          <#-- TESTING -->
            <#include "gemini/__metadataqualityResults.ftl">
          <#-- END OF TESTING -->

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