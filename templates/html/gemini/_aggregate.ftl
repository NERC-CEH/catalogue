<#if children??>
<#assign childDatasets = func.filter(children, "associationType", "dataset") + func.filter(children, "associationType", "nonGeographicDataset") + func.filter(children, "associationType", "application") + func.filter(children, "associationType", "signpost")>
<#assign childSeries = func.filter(children, "associationType", "series") + func.filter(children, "associationType", "aggregate") + func.filter(children, "associationType", "collection")>
<#assign childServices = func.filter(children, "associationType", "service")>

	<#if childDatasets?size!=0 || childSeries?size!=0 || childServices?size!=0>
	<div id="section-children">
	<h3>Access the data <#if childServices?size!=0>and services</#if></h3>
		<#list childSeries?sort_by("title")>
			<div class="children">
			<h4>Data collections</h4>
			<#items as child>
				<div class="childRecord">
					<a href="${child.href}">${child.title}</a>
				</div>
			</#items>
			</div>
		</#list>
		<#list childDatasets?sort_by("title")>
			<div class="children">
			<h4>Datasets</h4>
			<#items as child>
				<div class="childRecord">
					<a href="${child.href}">${child.title}</a>
				</div>
			</#items>
			</div>
		</#list>
		<#list childServices?sort_by("title")>
			<div class="children">
		<h4>Web services</h4>
			<#items as child>
				<div class="childRecord">
					<a href="${child.href}">${child.title}</a>
				</div>
			</#items>
			</div>
		</#list>	
	</div>
	</#if>
	
</#if>
<#include "_supplemental.ftl">