<#assign
	relatedDatasets = func.filter(supplemental, "function", "relatedDataset")
	otherSupplemental = func.filter(supplemental, "function", "relatedDataset", true)
	>

<#assign apps = func.filter(onlineResources, "function", "browser") >

<div class="py-3">

  <#if title?? && title?has_content>
    <#include "_title.ftlh">
  </#if>

  <#if description??>
    <#include "_description.ftlh">
  </#if>

  <#if rel_hasMember?has_content && rel_hasMember?size gt 0 >
    <div id="segment-itemren">
      <h2>
      This ${recordType} contains <#if rel_hasMember?size gt 1>these<#else>this</#if> resource<#if rel_hasMember?size gt 1>s</#if></h2>

      <div class="ukcehcards">
        <#list rel_hasMember>
            <#items as item>
              <#if item.associationType = 'dataset' ||  item.associationType = 'nonGeographicDataset' ||  item.associationType = 'signpost'>
                <#assign type="Dataset" icon="fa-solid fa-chart-column" >
              <#elseif item.associationType = "series" || item.associationType = "collection" || item.associationType = "aggregate">
                <#assign type="Data collection" icon="fa-layer-group" >
              <#elseif item.associationType = "application">
                <#assign type="Model code" icon="fa-terminal" >
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

  <#if rel_memberOf?has_content && rel_memberOf?size gt 0 >
    <h2>This ${recordType} is included in the following collection<#if rel_memberOf?size gt 1>s</#if></h2>
    <div class="ukcehcards">
      <#list rel_memberOf?sort_by("title") as item>
        <@card item.href item.publicationStatus item.availability 'Data collection' item.title/>
      </#list>
    </div>
  </#if>

  <#if otherSupplemental?has_content && otherSupplemental?size gt 0>
    <div id="segment-supplemental">
      <h2>Supplemental information</h2>

      <#list otherSupplemental as supplement>
        <@func.displaySupplemental supplement true/>
      </#list>

    </div>
  </#if>

</div>

<#macro card address publicationStatus availability type title icon='fa-layer-group' external=false>
  <#assign target = "">
  <#assign cardClass = " ukcehcard-" + publicationStatus?lower_case >

  <#if availability?has_content && availability != "Unknown">
   <#assign cardClass += " ukcehcard-" + availability?lower_case>
	</#if>

	<#if address?has_content && address?matches("^http(|s)://.+")>
		<#if external=true>
			<#assign target="target='_blank' rel='noopener noreferrer'">
		</#if>

    <#if publicationStatus!="published" && !permission.userCanEdit(id)>
      <#assign cardClass+="hidden">
    </#if>
		<a class="ukcehcard ${cardClass}" href="${address}" ${target}>

			<div class="ukcehcard-icon">
				<span class="fa-stack fa-xl">
					<i class="fa-solid fa-circle fa-stack-2x"></i>
					<i class="fa-solid ${icon} fa-stack-1x fa-inverse"></i>
				</span>
			</div>
			<div>
				<div class="ukcehcard-type"><small><span>${publicationStatus}</span>${type}</small></div>
				<div class="ukcehcard-content">${title}</div>
			</div>
		</a>
	</#if>
</#macro>
