<#assign
	otherSupplemental = nonRelatedDatasets
	apps = browsingApps
>

<div class="py-3">

  <#if title?? && title?has_content>
    <#include "_title.ftlh">
  </#if>

  <#include "_aggregateAccessButtons.ftlh">

  <#if description??>
    <#include "_description.ftlh">
  </#if>

  <#if apps?? && apps?size gte 1>
    <#include "_spatialExplorer.ftlh">
  </#if>

  <#if rel_hasMember?has_content && rel_hasMember?size gt 0 >
    <div id="segment-members">
      <h2>
      This collection contains <#if rel_hasMember?size gt 1>these<#else>this</#if> resource<#if rel_hasMember?size gt 1>s</#if></h2>

      <div class="ukcehcards">
        <#list rel_hasMember>
            <#items as item>
              <#if item.associationType = 'dataset' ||  item.associationType = 'nonGeographicDataset' ||  item.associationType = 'signpost'>
                <#assign type="Dataset" icon="fa-solid fa-chart-column" >
              <#elseif item.associationType = "series" || item.associationType = "collection" || item.associationType = "aggregate">
                <#assign type="Data collection" icon="fa-layer-group" >
              <#elseif item.associationType = "model">
                <#assign type="Model" icon="fa-list-check" >
              <#elseif item.associationType = "software" || item.associationType = "application">
                <#assign type="Software" icon="fa-code" >
              <#elseif item.associationType = "computationalNotebook">
                <#assign type="Notebook" icon="fa-book-open" >
              <#elseif item.associationType = "method">
                <#assign type="Method" icon="fa-list-check">
              <#elseif item.associationType = "service">
                <#assign type="Web service" icon="fa-location-arrow" >
              <#else>
                <#assign type=item.associationType icon="" >
              </#if>

              <@card item.href item.publicationStatus item.availability type item.title icon/>
            </#items>
        </#list>

        <#if apps?has_content && apps?size gt 0>
          <#list apps as app>
            <@card app.url '' '' 'Application' app.name 'fa-solar-panel'/>
          </#list>
        </#if>
      </div>
    </div>
  </#if>

  <#if (rel_related?has_content && rel_hasMember?size gt 0) || (otherSupplemental?has_content && otherSupplemental?size gt 0) >
    <div id="segment-related" class="mt-5">

      <h3 class="my-3">Other related resources</h3>

      <div class="ukcehcards ukcehcards-small">
        <#list rel_related>
          <#items as item>
            <#if item.associationType = 'dataset' ||  item.associationType = 'nonGeographicDataset' ||  item.associationType = 'signpost'>
              <#assign type="Dataset" icon="fa-solid fa-chart-column" >
            <#elseif item.associationType = "series" || item.associationType = "collection" || item.associationType = "aggregate">
              <#assign type="Data collection" icon="fa-layer-group" >
            <#elseif item.associationType = "model">
              <#assign type="Model" icon="fa-list-check" >
            <#elseif item.associationType = "software">
              <#assign type="Software" icon="fa-code" >
            <#elseif item.associationType = "computationalNotebook">
              <#assign type="Notebook" icon="fa-book-open" >
            <#elseif item.associationType = "method">
              <#assign type="Method" icon="fa-list-check">
            <#elseif item.associationType = "service">
              <#assign type="Web service" icon="fa-location-arrow" >
            <#else>
              <#assign type=item.associationType  >
            </#if>

            <@card item.href item.publicationStatus item.availability type item.title icon/>
          </#items>
        </#list>

        <#list otherSupplemental>
          <#items as item>
            <@card item.url "published" "available" "Website" item.name "fa-globe" true />
          </#items>
        </#list>

      </div>
    </div>
  </#if>

  <#if rel_memberOf?has_content && rel_memberOf?size gt 0 >
    <h2>This ${recordType} is included in the following collection<#if rel_memberOf?size gt 1>s</#if></h2>
    <div class="ukcehcards">
      <#list rel_memberOf?sort_by("title") as item>
        <@card item.href item.publicationStatus item.availability 'Data collection' item.title/>
      </#list>
    </div>
  </#if>

</div>

<#macro card address publicationStatus availability type title icon='fa-globe' external=false >
  <#assign target = "">
  <#if external=true>
			<#assign target="target='_blank' rel='noopener noreferrer'">
	</#if>

  <#assign cardClass = " ukcehcard-" + publicationStatus?lower_case >
  <#if availability?has_content && availability != "Unknown">
   <#assign cardClass += " ukcehcard-" + availability?lower_case>
	</#if>

  <#assign visible = false>
  <#if publicationStatus=="published" || permission.userCanEdit(id)>
     <#assign visible = true>
  </#if>

  <#if visible>
    <#if address?has_content && address?matches("^http(|s)://.+")>
      <a class="ukcehcard ${cardClass}" href="${address}" ${target}>

        <div class="ukcehcard-icon">
          <span class="fa-stack fa-xl">
            <i class="fa-solid fa-circle fa-stack-2x"></i>
            <i class="fa-solid ${icon} fa-stack-1x fa-inverse"></i>
          </span>
        </div>
        <div class="ukcehcard-content">
          <div class="ukcehcard-type"><span>${publicationStatus}</span>${type}</div>
          <div class="ukcehcard-title">${title}</div>
        </div>
      </a>
    </#if>
  </#if>
</#macro>
