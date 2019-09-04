<#if children??>
	<div id="section-children">
		<#list children?sort_by("title")>
			<div class="children">
			<#items as child>
				<#if child.associationType = 'dataset'>
					<#assign type="Dataset" icon="fas fa-table" >
				<#elseif child.associationType = "series" || child.associationType = "collection">
					<#assign type="Data collection" icon="far fa-clone" >
				<#elseif child.associationType = "application">
					<#assign type="Model code" icon="fas fa-terminal" >
				<#elseif child.associationType = "service">
					<#assign type="Web service" icon="fas fa-location-arrow" >
				<#else>
					<#assign type=child.associationType icon="" >
				</#if>
				<a href="${child.href}" class="childRecord">
					<div><i class="${icon}"></i> <span>${type}</span></div>
					<div>${child.title}</div>
				</a>
			</#items>
			</div>
		</#list>
	</div>
</#if>
<#include "_supplemental.ftl">