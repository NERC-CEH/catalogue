<#function filter things name value not>
    <#local result = []>
    <#list things as thing>

		<#if not == 0>
		<#if thing[name] == value>
            <#local result = result + [thing]>
        </#if>
		</#if>

		<#if not == 1>
		<#if thing[name] != value>
            <#local result = result + [thing]>
        </#if>
		</#if>

		</#list>
    <#return result>
</#function>
<#assign authors = filter(responsibleParties, "role", "Author", 0) >
<#assign otherContacts = filter(responsibleParties, "role", "Author", 1) >

<#import "skeleton.html.tpl" as skeleton>

<@skeleton.master title=title>
<div id="metadata" class="container" prefix="dc: http://purl.org/dc/terms/ dcat: http://www.w3.org/ns/dcat#">
	<#include "_title.html.tpl">
	<#include "_notCurrent.html.tpl">
	<#include "_authorsTop.html.tpl">
	<#include "_description.html.tpl">
	<#include "_dates.html.tpl">
	
	<div class="row">
        <div class="col-sm-4 pull-right">
			<#include "_ordering.html.tpl">
		</div>
		<div class="col-sm-8">
			<#include "_extent.html.tpl">
			<#include "_onlineResources.html.tpl">
			<#include "_quality.html.tpl">
			
			<div id="section-access">
			<h3><a id="access"></a>Access information</h3>
			<#include "_accessInfo.html.tpl">	
			</div>
			
			<#include "_related.html.tpl">
			<#include "_authors.html.tpl">
			<#include "_otherContacts.html.tpl">
			<#include "_keywords.html.tpl">

			<#include "_uris.html.tpl">
			<#include "_spatial.html.tpl">
		</div>
  </div>
  
  <#include "_metadata.html.tpl">

</div>

</@skeleton.master>