<#import "blocks.ftlh" as blocks>
<#import "skeleton.ftlh" as skeleton>
<#import "../underscore.ftlh" as _>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
  <div id="metadata" class="metadata container">
    <@blocks.title title type />
    <@blocks.description description!"" />
    <div class="row">
      <div class="col-sm-4 col-sm-push-8">
        <#include "imp/_admin.ftlh">
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
</@skeleton.master>
