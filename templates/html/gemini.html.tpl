<#import "skeleton.html.tpl" as skeleton>

<@skeleton.master title=title>
<div id="metadata" class="container" prefix="dc: http://purl.org/dc/terms/ dcat: http://www.w3.org/ns/dcat#">
  <#include "_title.html.tpl">
  <#include "_notCurrent.html.tpl">
   <#include "_citation2.html.tpl">
 <#include "_description.html.tpl">

	<div class="row">
        <div class="col-sm-4 pull-right">
			<!--<#include "_browsegraphic.html.tpl">-->
			<#include "_ordering.html.tpl">
			<#include "_citation.html.tpl">
		</div>
		<div class="col-sm-8">
			<#include "_extent.html.tpl">
			<#include "_accessInfo.html.tpl">
			<#include "_quality.html.tpl">
			<#include "_links.html.tpl">
			<#include "_authors.html.tpl">
			<#include "_otherContacts.html.tpl">
			<#include "_keywords.html.tpl">

			<#include "_onlineResources.html.tpl">
			<#include "_uris.html.tpl">
			<#include "_spatial.html.tpl">
		</div>
  </div>
  <#include "_dates.html.tpl">
  <#include "_metadata.html.tpl">

</div>

</@skeleton.master>