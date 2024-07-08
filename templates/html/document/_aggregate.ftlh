<#assign
	relatedDatasets = func.filter(supplemental, "function", "relatedDataset")
	otherSupplemental = func.filter(supplemental, "function", "relatedDataset", true)
	>
<#if rel_hasMember?has_content && rel_hasMember?size gt 0 >
	<div id="section-itemren">
		<h2>
		This ${recordType} contains <#if rel_hasMember?size gt 1>these<#else>this</#if> resource<#if rel_hasMember?size gt 1>s</#if></h2>

		<#list rel_hasMember>
			<div class="cards">
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

				<@card item.href item.publicationStatus icon type item.title/>

			</#items>

			<#assign relatedDatasets = func.filter(supplemental, "function", "relatedDataset")>
			<#if relatedDatasets?has_content && relatedDatasets?size gt 0 >
				<#list relatedDatasets as item>
					<@card item.url 'fa-square-up-right' 'Dataset (external)' item.name true/>
				</#list>
			</#if>

			</div>
		</#list>
	</div>
</#if>

<#if rel_memberOf?has_content && rel_memberOf?size gt 0 >
	<h2>This ${recordType} is included in the following collection<#if rel_memberOf?size gt 1>s</#if></h2>
	<div class="cards">
		<#list rel_memberOf?sort_by("title") as item>
			<@card item.href item.publicationStatus 'fa-layer-group' 'Data collection' item.title/>

		</#list>
	</div>
</#if>


<#if otherSupplemental?has_content && otherSupplemental?size gt 0>
  <div id="section-supplemental">
    <h2>Supplemental information</h2>

    <#list otherSupplemental as supplement>
      <@func.displaySupplemental supplement true/>
    </#list>

  </div>
</#if>

<#macro card address publicationStatus icon type title external=false>
  <#assign target=""
           cardClass=publicationStatus>

	<#if address?has_content && address?matches("^http(|s)://.+")>
		<#if external=true>
			<#assign target="target='_blank' rel='noopener noreferrer'">
		</#if>

    <#if publicationStatus!="published" && !permission.userCanEdit(id)>
      <#assign cardClass="hidden">
    </#if>
		<a href="${address}" class="card card-${cardClass}" ${target}>
			<div class="card-icon">
				<span class="fa-stack fa-2x">
					<i class="fa-solid fa-square fa-stack-2x"></i>
					<i class="fa-solid ${icon} fa-stack-1x fa-inverse"></i>
				</span>
			</div>
			<div>
				<div class="card-type"><span>${publicationStatus}</span>${type}</div>
				<div class="card-title">${title}</div>
			</div>
		</a>
	</#if>
</#macro>
