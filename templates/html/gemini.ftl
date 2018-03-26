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
<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue) rdf="${uri}?format=ttl" schemaorg="${uri}?format=schema.org" canonical="${uri}">
  <div id="metadata">
   <div class="container">
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
            <#include "gemini/_quality.ftl">
            <#include "gemini/_authors.ftl">
            <#include "gemini/_otherContacts.ftl">
            <#include "gemini/_spatial.ftl">
            <#include "gemini/_keywords.ftl">
            <#include "gemini/_uris.ftl">
          </div>
        </div>
      <#else>
        <#include "gemini/_aggregate.ftl">
      </#if>
     
      <#include "gemini/_metadata.ftl">
    </div>
	  <div id="footer">
	    <#include "gemini/_footer.ftl">
	  </div>
  </div>
  <script type="application/ld+json">
	  <#include "../schema.org/schema.org.tpl">
  </script>

</@skeleton.master>