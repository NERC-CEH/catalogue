<#if rel_hasMember??>
	<div id="section-children">
		<#list rel_hasMember?sort_by("title")>
			<div class="aggregate-children">
			<#items as child>
				<#if child.associationType = 'dataset' ||  child.associationType = 'nonGeographicDataset' ||  child.associationType = 'signpost'>
					<#assign type="Dataset" icon="fas fa-table" >
				<#elseif child.associationType = "series" || child.associationType = "collection" || child.associationType = "aggregate">
					<#assign type="Data collection" icon="far fa-clone" >
				<#elseif child.associationType = "application">
					<#assign type="Model code" icon="fas fa-terminal" >
				<#elseif child.associationType = "service">
					<#assign type="Web service" icon="fas fa-location-arrow" >
				<#else>
					<#assign type=child.associationType icon="" >
				</#if>
				<a href="${child.href}" class="aggregate-child">
					<div class="aggregate-child--icon"><i class="${icon}"></i></div>
					<div>
						<div class="aggregate-child--type">${type}</div>
						<div class="aggregate-child--title">${child.title}</div>
					</div>
				</a>
			</#items>
			</div>
		</#list>
	</div>
</#if>

<#if rel_memberOf?has_content && rel_memberOf?size gt 0>
	<div id="associations-parents" class="aggregate-children">
		<#list rel_memberOf as item>
			<a href="${item.href?html}" class="aggregate-child">
				<div class="aggregate-child--icon"><i class="fas fa-level-up-alt"></i></div>
				<div>
					<div class="aggregate-child--type">This ${recordType} is part of</div>
					<div class="aggregate-child--title">${item.title?html}</div>
				</div>
			</a>
		</#list>
	</div>
</#if>


<#include "_supplemental.ftl">
