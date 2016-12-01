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
        <@blocks.links jena.datasets(uri) "Datasets" />
        <@blocks.links jena.models(uri) "Models" />
      </div>
      <div class="col-sm-8 col-sm-pull-4">
        <dl class="dl-horizontal">
          <#if date?has_content>
          <dt>Date</dt><dd>${date}</dd>
          </#if>
          <#if studySite?has_content>
          <dt>Study Site</dt><dd>${studySite}</dd>
          </#if>
          <#if objective?has_content>
          <dt>Objective</dt><dd>${objective}</dd>
          </#if>
          <#if funderDetails?has_content>
          <dt>Funder Details</dt><dd>${funderDetails}</dd>
          </#if>
          <#if contact??>
            <dt>Contact</dt>
            <dd>
              <#if contact.individualName?has_content>${contact.individualName}</#if><#if contact.organisationName?has_content>, ${contact.organisationName}</#if>
            </dd>
          </#if>
        </dl>
        <#if inputData?? >
          <h3>Input Data</h3>
          <ul>
          <#list inputData as input>
            <li>${input}</li>
          </#list>
          </ul>
        </#if>
        <#if associatedResources?? >
          <h3>Associated Resources</h3>
          <ul>
          <#list associatedResources as resource>
            <li>${resource.associationType!} <a href="${resource.href!}">${resource.title!resource.href}</a></li>
          </#list>
          </ul>
        </#if>
      </div>
    </div>
</#escape>
</@skeleton.master>
