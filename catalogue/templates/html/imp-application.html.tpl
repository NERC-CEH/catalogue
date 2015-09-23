<#import "blocks.html.tpl" as blocks>
<#import "skeleton.html.tpl" as skeleton>
<#import "../underscore.tpl" as _>

<@skeleton.master title=title>
<#escape x as x?html>
  <div id="metadata" class="container">

    <@blocks.title title type />
    <@blocks.description description />
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
      <#if (modellerDetails?? && (modellerDetails.name?? || modellerDetails.organisation??)) >
      <dt>Modeller</dt><dd>${modellerDetails.name}, ${modellerDetails.organisation}</dd>
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
        <li>${resource.associationType!} <a href="${resource.href!}">${resource.value!resource.href}</a></li>
      </#list>
      </ul>
    </#if>
</#escape>
</@skeleton.master>
