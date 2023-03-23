<#if rel_hasMember?has_content && rel_hasMember?size gt 0 >
	<div id="section-children">
		<h2>
		This ${recordType} contains <#if rel_hasMember?size gt 1>these<#else>this</#if> resource<#if rel_hasMember?size gt 1>s</#if></h2>

		<#list rel_hasMember?sort_by("title")>
			<div class="cardlist">
			<#items as child>
				<#if child.associationType = 'dataset' ||  child.associationType = 'nonGeographicDataset' ||  child.associationType = 'signpost'>
					<#assign type="Dataset" icon="fas fa-file-contract" >
				<#elseif child.associationType = "series" || child.associationType = "collection" || child.associationType = "aggregate">
					<#assign type="Data collection" icon="far fa-clone" >
				<#elseif child.associationType = "application">
					<#assign type="Model code" icon="fas fa-terminal" >
				<#elseif child.associationType = "service">
					<#assign type="Web service" icon="fas fa-location-arrow" >
				<#else>
					<#assign type=child.associationType icon="" >
				</#if>
				<a href="${child.href}" class="cardlist--item">
					<div class="icon"><i class="${icon}"></i></div>
					<div>
						<div class="type">
							${type} 
						</div>
						<div class="title">${child.title}</div>
					</div>
				</a>
			</#items>
			</div>
		</#list>
	</div>
</#if>

<#if rel_memberOf?has_content && rel_memberOf?size gt 0 >
	<h2>This ${recordType} is included in the following collection<#if rel_memberOf?size gt 1>s</#if></h2>
	<div id="parents">
		<div class="cardlist">
			<#list rel_memberOf?sort_by("title") as item>
				<a href="${item.href}" class="cardlist--item">
					<div class="icon"><i class="far fa-clone"></i></div>
					<div>
						<div class="type">Data collection</div>
						<div class="title">${item.title}</div>
					</div>
				</a>
			</#list>
		</div>
	</div>
</#if>

<#include "_supplemental.ftlh">
