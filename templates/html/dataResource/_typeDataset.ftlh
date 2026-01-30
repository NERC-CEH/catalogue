<#if isLinkedDoc>
  <#include "_linked.ftlh">
</#if>

<#if resourceType.value == 'nercSignpost' || resourceType.value == 'signpost'>
  <@displayDataResource/>
<#else>
  <@displayDataResource true/>
</#if>

<#macro displayDataResource toc=false>

  <div class="row" data-bs-spy="scroll" data-bs-target="#tableOfContents"  data-bs-root-margin="0px 0px -70% 0px" data-bs-smooth-scroll="true">

    <#assign mainColClasses="">

    <#if toc>
      <#assign mainColClasses="content">
      <#include "_toc.ftlh">
    </#if>

    <div class="${mainColClasses} py-3">

      <#if availability?? && availability?has_content && availability != "Available" && availability != "Controlled"  && availability != "Unknown"  >
        <div class="alert alert-${availability}" role="alert">
          <#include "_status.ftlh">
        </div>
      </#if>

      <#if rel_supersedes?has_content && rel_supersedes?size gt 0>
        <#include "_latestVersion.ftlh">
      </#if>

      <#if title?? && title?has_content>
        <#include "_title.ftlh">
      </#if>

      <#include "_accessButtons.ftlh">

      <@viewAs />

      <#if description??>
        <#include "_description.ftlh">
      </#if>

      <#if !isLinkedDoc>
        <#include "_uploadData.ftlh">

        <#if resourceType.value == 'nercSignpost' || resourceType.value == 'signpost'>
          <#include "_distribution_signpost.ftlh">
        </#if>

        <#if resourceType.value == 'thirdPartyDataset'>
          <#include "_third_party.ftlh">
        </#if>
      </#if>

      <#include "_formats.ftlh">
      <#if resourceType.value !='nonGeographicDataset'><#include "_spatial.ftlh"></#if>
      <#include "_temporal.ftlh">
      <#include "_dataquality.ftlh">
      <#include "_constraints.ftlh">
      <#include "_related.ftlh">
      <#include "_citation_list.ftlh">
      <#include "_supplemental.ftlh">
      <#include "_contacts.ftlh">

      <#if topicCategories?? || allKeywords?has_content || inspireTheme?? || funding?? >
        <div id="segment-additional">
          <h2>Additional metadata</h2>
          <#include "_tags.ftlh">
          <#include "_funding.ftlh">
        </div>
      </#if>

    </div>

  </div>
</#macro>

<#macro viewAs>
 <div class="d-flex justify-content-end py-2">
  <div class="dropdown small">
      <a class="dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
        View record as
      </a>
      <ul class="dropdown-menu dropdown-menu-end">
        <li><a class="small dropdown-item" href="${uri}?format=json">json</a></li>
        <#if resourceType?? && (resourceType.value == 'dataset' || resourceType.value == 'model' || resourceType.value == 'software' || resourceType.value == 'service' )>
          <li><a class="small dropdown-item" href="${uri}.xml?format=gemini">ISO 19115 (XML)</a></li>
          <li><a class="small dropdown-item" href="${uri}?format=schema.org">schema.org (json-LD)</a></li>
          <li><a class="small dropdown-item" href="${uri}?format=ttl">RDF (Turtle)</a></li>
        </#if>
      </ul>
    </div>
  </div>
</#macro>
