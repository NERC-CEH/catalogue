<#if rel_hasMember??>
	<div id="section-children">
	<h2>Contents of this collection:</h2>
		<#list rel_hasMember?sort_by("title")>
			<div class="grid">
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
				<a href="${child.href}" class="card">
					<div><div><i class="${icon}"></i></div><div><span>${type}</span></div></div>
					<div >${child.title}</div>
				</a>
			</#items>
			</div>
		</#list>
	</div>
</#if>

<#if rel_memberOf?has_content && rel_memberOf?size gt 0>
	<div id="associations-parents" class="aggregate-children">
	<h2>This ${recordType} is part of:</h2>
		<div class="grid">
			<#list rel_memberOf as item>
				<a href="${item.href?html}" class="card">
					<div><i class="fas fa-level-up-alt"></i></div>
					<div> ${item.title?html}</div>
				</a>
			</#list>
		</div>
	</div>
</#if>

<#include "_supplemental.ftl">
