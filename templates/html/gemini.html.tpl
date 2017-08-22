<#import "blocks.html.tpl" as blocks>
<#import "skeleton.html.tpl" as skeleton>
<#import "../underscore.tpl" as _>

<#assign authors       = _.filter(responsibleParties, _.isAuthor) >
<#assign otherContacts = _.reject(responsibleParties, _.isAuthor) >
<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue) rdf="${uri}?format=rdf">
  <div id="metadata">
   <div class="container">
	  <div id="section-Top">
		  <@blocks.title title=title!"" type=(resourceType.value)!"" />
		  <#include "gemini/_licence.html.tpl">
		  <#include "gemini/_notCurrent.html.tpl">
		  <#include "gemini/_authorsTop.html.tpl">
	  </div>
      <@blocks.description description!"" />
      <#include "gemini/_dates.html.tpl">

      <div class="row">
        <div class="col-sm-4 col-sm-push-8">
          <#include "gemini/_admin.html.tpl">
          <#include "gemini/_uploadData.html.tpl">
          <#include "gemini/_distribution.html.tpl">
          <#include "gemini/_reuse.html.tpl">
          <#include "gemini/_children.html.tpl">
          <#include "gemini/_related.html.tpl">
          <#include "gemini/_model.html.tpl">
        </div>
        <div class="col-sm-8 col-sm-pull-4">
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
  </div>
</@skeleton.master>