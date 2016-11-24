<#import "blocks.html.tpl" as blocks>
<#import "skeleton.html.tpl" as skeleton>
<#import "../underscore.tpl" as _>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
<#escape x as x?html>
  <div id="metadata" class="container">
    <@blocks.title title type />
    <@blocks.description description!"" />
    <div class="row">
      <div class="col-sm-4 col-sm-push-8">
        <#include "imp/_admin.html.tpl">
      </div>
      <div class="col-sm-8 col-sm-pull-4">
        <dl class="dl-horizontal">
        <#if caseStudy?? >
          <dt>Link</dt><dd><a href="${caseStudy.href}">${caseStudy.title}</a></dd>
        </#if>
        <#if contact??>
          <dt>Contact</dt>
          <dd>
            <#if contact.individualName?has_content>${contact.individualName}</#if><#if contact.organisationName?has_content>, ${contact.organisationName}</#if>
          </dd>
        </#if>
        </dl>
      </div>
    </div>
  </div>
</#escape>
</@skeleton.master>
