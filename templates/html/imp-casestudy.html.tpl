<#import "blocks.html.tpl" as blocks>
<#import "skeleton.html.tpl" as skeleton>
<#import "../underscore.tpl" as _>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
<#escape x as x?html>
  <div id="metadata" class="container">
    <@blocks.title title type />
    <@blocks.description description!"" />
    <div class="row">
      <div class="col-sm-4 col-xs-12 col-sm-push-8">
        <#include "imp/_admin.html.tpl">
      </div>
      <div class="col-sm-8 col-xs-12 col-sm-pull-4">
      <p>Link to case study</p>
      </div>
    </div>
  </div>
</#escape>
</@skeleton.master>
