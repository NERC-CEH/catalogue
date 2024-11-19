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
        <div class="row key-value">
        <#if caseStudy?? >
          <div class="key col-sm-3">Link</div><div class="value col-sm-9"><a href="${caseStudy.href}">${caseStudy.title}</a></div>
        </#if>
        <#if contact??>
          <div class="key col-sm-3">Contact</div>
          <div class="value col-sm-9">
            <#if contact.individualName?has_content>${contact.individualName}</#if><#if contact.organisationName?has_content>, ${contact.organisationName}</#if>
          </div>
        </#if>
        </div>
      </div>
    </div>
  </div>
</@skeleton.master>
