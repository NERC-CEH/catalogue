<#import "blocks.html.tpl" as blocks>
<#import "skeleton.html.tpl" as skeleton>
<#import "../underscore.tpl" as _>

<#assign authors       = _.filter(responsibleParties, _.isAuthor) >
<#assign otherContacts = _.reject(responsibleParties, _.isAuthor) >
<@skeleton.master title=title rdf="${uri}?format=rdf">
  <div id="metadata" class="container">

    <@blocks.title title resourceType.value />
    <#include "gemini/_notCurrent.html.tpl">
    <#include "gemini/_authorsTop.html.tpl">
    <@blocks.description description />
    <#include "gemini/_dates.html.tpl">
    
    <div class="row">
      <div class="col-sm-4 col-xs-12 pull-right">
        <#include "gemini/_distribution.html.tpl">
        <#include "gemini/_children.html.tpl">
        <#include "gemini/_related.html.tpl">
        <#include "gemini/_actions.html.tpl">
      </div>
      <div class="col-sm-8 col-xs-12">
        <#include "gemini/_extent.html.tpl">
        <#include "gemini/_onlineResources.html.tpl">
        <#include "gemini/_quality.html.tpl">

        <#include "gemini/_authors.html.tpl">
        <#include "gemini/_otherContacts.html.tpl">
        <#include "gemini/_spatial.html.tpl">
        <#include "gemini/_keywords.html.tpl">
        <#include "gemini/_uris.html.tpl">
      </div>
    </div>
    
    <#include "gemini/_metadata.html.tpl">
	
  </div>
	
	<div id="footer">
	    <#include "gemini/_footer.html.tpl">
	</div>
</@skeleton.master>