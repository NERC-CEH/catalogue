<<<<<<< HEAD
=======
<#import "blocks.html.tpl" as blocks>
>>>>>>> develop
<#import "skeleton.html.tpl" as skeleton>
<#import "../underscore.tpl" as _>

<#assign authors       = _.filter(responsibleParties, _.isAuthor) >
<#assign otherContacts = _.reject(responsibleParties, _.isAuthor) >
<@skeleton.master title=title rdf="${uri}?format=rdf">
<<<<<<< HEAD
  <div id="metadata">
    <div class="container">

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
      <#include "metadata/_json.html.tpl">
  </div>
=======
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
>>>>>>> develop
</@skeleton.master>
