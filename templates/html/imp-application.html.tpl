<#import "blocks.html.tpl" as blocks>
<#import "skeleton.html.tpl" as skeleton>
<#import "../underscore.tpl" as _>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
<#escape x as x?html>
  <div id="metadata" class="container">

    <@blocks.title title type />
    <@blocks.description description!"" />
    <div class="row">
      <div class="col-sm-4 col-xs-12 pull-right">
        <#include "imp/_admin.html.tpl">
        <#if links?? >
          <#list links>
            <div class="panel panel-default" id="document-order">
              <div class="panel-heading"><p class="panel-title">Datasets</p></div>
              <div class="panel-body">
                <ul class="list-unstyled">
                  <#items as link>
                  <li><a href="${link.href}">${link.title}</a></li>
                  </#items>
                </ul>
              </div>
            </div>
          </#list>
        </#if>
        <#if model?? >
          <div class="panel panel-default" id="document-order">
            <div class="panel-heading"><p class="panel-title">Model</p></div>
            <div class="panel-body">
              <ul class="list-unstyled">
                <li><a href="${model.href?html}">${model.title?html}</a></li>
              </ul>
            </div>
          </div>
        </#if>
      </div>
      <div class="col-sm-8 col-xs-12">
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
            <li>${resource.associationType!} <a href="${resource.href!}">${resource.title!resource.href}</a></li>
          </#list>
          </ul>
        </#if>
      </div>
    </div>
</#escape>
</@skeleton.master>
