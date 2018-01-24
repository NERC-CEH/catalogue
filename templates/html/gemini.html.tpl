<#import "blocks.html.tpl" as blocks>
<#import "skeleton.html.tpl" as skeleton>
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
    <#include "gemini/_admin.html.tpl">
	  <div id="section-Top">
      <#include "gemini/_title.html.tpl">
	  </div>
      <@blocks.description description!"" />
      <#if resourceType.value != 'aggregate' && resourceType.value != 'collection'>
        <#include "gemini/_dates.html.tpl">
        <div class="row">
          <div class="col-sm-4 col-sm-push-8">
            <#include "gemini/_uploadData.html.tpl">
            <#include "gemini/_distribution.html.tpl">
            <#include "gemini/_reuse.html.tpl">
            <#include "gemini/_children.html.tpl">
            <#include "gemini/_related.html.tpl">
            <#include "gemini/_model.html.tpl">
          </div>
          <div class="col-sm-8 col-sm-pull-4">
            <#include "gemini/_extent.html.tpl">
            <#include "gemini/_supplemental.html.tpl">
            <#include "gemini/_quality.html.tpl">
            <#include "gemini/_authors.html.tpl">
            <#include "gemini/_otherContacts.html.tpl">
            <#include "gemini/_spatial.html.tpl">
            <#include "gemini/_keywords.html.tpl">
            <#include "gemini/_uris.html.tpl">
          </div>
        </div>
      <#else>
        <#include "gemini/_aggregate.html.tpl">
      </#if>
     
      <#include "gemini/_metadata.html.tpl">
    </div>
	  <div id="footer">
	    <#include "gemini/_footer.html.tpl">
	  </div>
  </div>
  <script type="application/ld+json">
	  <#include "../schema.org/schema.org.tpl">
  </script>

</@skeleton.master>