<#if children??>
<div id="section-children">
<#assign childDatasets = func.filter(children, "associationType", "dataset") + func.filter(children, "associationType", "nonGeographicDataset") + func.filter(children, "associationType", "application")>
<#assign childSeries = func.filter(children, "associationType", "series")>
<#assign childData = func.filter(children, "associationType", "dataset") + func.filter(children, "associationType", "nonGeographicDataset") + func.filter(children, "associationType", "application")>

<#assign childServices = func.filter(children, "associationType", "service")>

<h3>Access the data <#if childServices?size!=0>and services</#if></h3>
	<#list childDatasets>
		<div class="children">
		<h4>Datasets</h4>
		<#items as child>
			<div class="childRecord">
				<a href="${child.href}">${child.title}</a>
			</div>
		</#items>
		</div>
	</#list>
	<#list childSeries>
		<div class="children">
		<h4>Data series</h4>
		<#items as child>
			<div class="childRecord">
				<a href="${child.href}">${child.title}</a>
			</div>
		</#items>
		</div>
	</#list>
	<#list childServices>
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