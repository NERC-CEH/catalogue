<#if isLinkedDoc>
  <#include "_linked.ftlh">
</#if>

<#if resourceType.value == 'nercSignpost' || resourceType.value == 'signpost'>
  <@displayDataResource/>
<#else>
  <@displayDataResource true/>
</#if>

<#macro displayDataResource toc=false>

  <div class="row" data-bs-spy="scroll" data-bs-root-margin="0px 0px -25%" data-bs-target="#tableOfContents" data-bs-smooth-scroll="true">

    <#assign mainColClasses="">

    <#if toc>
      <#assign mainColClasses="col-xl-9 col-lg-8 col-md-7 ms-4-md">
      <#include "_toc.ftlh">
    </#if>

    <div class="${mainColClasses} py-3">

      <#if title?? && title?has_content>
        <#include "_title.ftlh">
      </#if>

      <#include "_newButtons.ftlh">

      <div class="topDOI"><#include "_citation.ftlh"></div>

      <#if rel_supersedes?has_content && rel_supersedes?size gt 0>
        <#include "_latestVersion.ftlh">
      </#if>

      <#if description??>
        <#include "_description.ftlh">
      </#if>

      <#if !isLinkedDoc>
        <#include "_uploadData.ftlh">

        <#if metadata.catalogue == "eidc">
          <#include "_metrics.ftlh">
        </#if>

        <#if resourceType.value == 'service' && mapViewable>
          <#include "_distribution_service.ftlh">
        <#elseif resourceType.value == 'nercSignpost' || resourceType.value == 'signpost'>
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
      <#include "_related.ftlh">
      <#include "_citation_list.ftlh">
      <#include "_supplemental.ftlh">
      <#include "_contacts.ftlh">
      <#include "_constraints.ftlh">

      <#if topicCategories?? || descriptiveKeywords?? || inspireTheme?? || funding?? >
        <div id="segment-additional">
          <h2>Additional metadata</h2>
          <#include "_tags.ftlh">
          <#include "_funding.ftlh">
          <#if metadataDateTime?has_content>
            <div class="row key-value">
              <div class="col-sm-3 key">Last updated</div>
              <div class="col-sm-9 value">${metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']}</div>
            </div>
          </#if>
        </div>
      </#if>

    </div>

  </div>
</#macro>
