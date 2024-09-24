<#import "blocks.ftlh" as blocks>
<#import "skeleton.ftlh" as skeleton>
<#import "../functions.ftlh" as func>

<#assign
  catalogue = catalogues.retrieve(metadata.catalogue)
  isLinkedDoc="false"
  linkedClass=""
>

<#if supplemental?has_content>
  <#assign
    referencedBy = func.filter(supplemental, "function", "isReferencedBy")
    supplementTo = func.filter(supplemental, "function", "isSupplementTo")
    websites = func.filter(supplemental, "function", "website")
    supOther = func.filter(supplemental, "function", "") + func.filter(supplemental, "function", "relatedArticle") + func.filter(supplemental, "function", "relatedDataset")
  >
</#if>

<#macro getLabel val array>
  <#list array as item>
    <#if item['value']==val>
      ${item['label']}
      <#break/>
    </#if>
  </#list>
</#macro>

<@skeleton.master title=title catalogue=catalogue>

<div id="metadata" class="metadata">
  <div class="container">
    <#include "document/_admin.ftlh">
    <div id="section-Top">
      <#include "document/_title.ftlh">
    </div>
    <#if description?? && description?has_content>
      <div id="document-description">
        <h2>Description</h2>
        <p>${description}</p>
        <#if emissionComponent?? && emissionComponent?has_content>
        <p>This record represents ${emissionComponent.format()} data.</p>
        </#if>
      </div>
    </#if>

    <#include "document/_supplemental.ftlh">

    <h2>Additional metadata</h2>
    <dl id="otherInfo" class="dl-horizontal">
      <#include "document/_tags.ftlh">
      <#include "document/_spatial.ftlh">
    </dl>


    <div>
      <p>
        This record is part of the UK Community Emissions Modelling
        System.  See <a href="https://www.uk-ems.org.uk/">here</a>
        for more information.
      </p>
  </div>
</div>

</@skeleton.master>
