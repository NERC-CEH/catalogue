<#import "skeleton.html.tpl" as skeleton>
<#import "../underscore.tpl" as _>

<#function isAuthor responsibleParty>
  <#return responsibleParty["role"] == "Author">
</#function>

<#assign authors       = _.filter(responsibleParties, isAuthor) >
<#assign otherContacts = _.reject(responsibleParties, isAuthor) >
<@skeleton.master title=title>
  <div id="metadata" class="container" prefix="dc: http://purl.org/dc/terms/ dcat: http://www.w3.org/ns/dcat#">

    <#include "_title.html.tpl">
    <#include "_notCurrent.html.tpl">
    <#include "_authorsTop.html.tpl">
    <#include "_description.html.tpl">
    <#include "_dates.html.tpl">
    
    <div class="row">
      <div class="col-sm-4 col-xs-12 pull-right">
        <#include "_children.html.tpl">
        <#include "_ordering.html.tpl">
      </div>
      <div class="col-sm-8 col-xs-12">
        <#include "_extent.html.tpl">
        <#include "_onlineResources.html.tpl">
        <#include "_quality.html.tpl">

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