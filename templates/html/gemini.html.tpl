<#import "skeleton.html.tpl" as skeleton>
<#import "../underscore.tpl" as _>

<#assign authors       = _.filter(responsibleParties, _.isAuthor) >
<#assign otherContacts = _.reject(responsibleParties, _.isAuthor) >
<@skeleton.master title=title>
  <div id="metadata" class="container" prefix="dc: http://purl.org/dc/terms/ v: http://www.w3.org/2006/vcard/ns# geo: http://www.opengis.net/ont/geosparql# dcat: http://www.w3.org/ns/dcat#" property="dcat:CatalogRecord" content="${uri}" about="${uri}">

    <#include "metadata/_title.html.tpl">
    <#include "metadata/_notCurrent.html.tpl">
    <#include "metadata/_authorsTop.html.tpl">
    <#include "metadata/_description.html.tpl">
    <#include "metadata/_dates.html.tpl">
    
    <div class="row">
      <div class="col-sm-4 col-xs-12 pull-right">
        <#include "metadata/_distribution.html.tpl">
        <#include "metadata/_children.html.tpl">
        <#include "metadata/_related.html.tpl">
        <#include "metadata/_actions.html.tpl">
      </div>
      <div class="col-sm-8 col-xs-12">
        <#include "metadata/_extent.html.tpl">
        <#include "metadata/_onlineResources.html.tpl">
        <#include "metadata/_quality.html.tpl">

        <#include "metadata/_authors.html.tpl">
        <#include "metadata/_otherContacts.html.tpl">
        <#include "metadata/_spatial.html.tpl">
        <#include "metadata/_keywords.html.tpl">
        <#include "metadata/_uris.html.tpl">
      </div>
    </div>
    
    <#include "metadata/_metadata.html.tpl">
	
  </div>
	
	<div id="footer">
	    <#include "metadata/_footer.html.tpl">
	</div>
</@skeleton.master>
