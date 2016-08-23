<#import "blocks.html.tpl" as blocks>
<#import "skeleton.html.tpl" as skeleton>
<#import "../underscore.tpl" as _>
<#import "gemini/_licence.html.tpl" as licence>

<#assign authors       = _.filter(original.responsibleParties, _.isAuthor) >
<#assign otherContacts = _.reject(original.responsibleParties, _.isAuthor) >
<@skeleton.master title=original.title catalogue=metadata.currentCatalogue rdf="${uri}?format=rdf">
  <div id="metadata">
   <div class="container">
    <div id="section-Top">
      <@blocks.title title=original.title!"" type=(original.resourceType.value)!"" />
      <@licence.licence original.useConstraints />
      <#-- <#include "gemini/_notCurrent.html.tpl">
      <#include "gemini/_authorsTop.html.tpl"> -->
    </div>
      <@blocks.description original.description!"" />
     <#--  <#include "gemini/_dates.html.tpl"> -->

      <div class="row">
        <div class="col-sm-4 col-xs-12 pull-right">
          <#-- <#include "gemini/_admin.html.tpl">
          <#include "gemini/_distribution.html.tpl">
          <#include "gemini/_reuse.html.tpl">
          <#include "gemini/_children.html.tpl">
          <#include "gemini/_related.html.tpl">
          <#include "gemini/_model.html.tpl"> -->
        </div>
        <div class="col-sm-8 col-xs-12">
          <#include "_linked.html.tpl">
          <#-- <#include "gemini/_repository.html.tpl">
          <#include "gemini/_extent.html.tpl">
          <#include "gemini/_onlineResources.html.tpl">
          <#include "gemini/_quality.html.tpl">

          <#include "gemini/_authors.html.tpl">
          <#include "gemini/_otherContacts.html.tpl">
          <#include "gemini/_spatial.html.tpl">
          <#include "gemini/_keywords.html.tpl">
          <#include "gemini/_uris.html.tpl"> -->
        </div>
      </div>
      <#include "gemini/_metadata.html.tpl">
    </div>
    <div id="footer">
      <#include "gemini/_footer.html.tpl">
    </div>
  </div>
</@skeleton.master>