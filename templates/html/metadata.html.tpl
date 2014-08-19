<#import "skeleton.html.tpl" as skeleton>

<@skeleton.master title=title>
<div id="metadata" class="container" prefix="dc: http://purl.org/dc/terms/ dcat: http://www.w3.org/ns/dcat#">
  <#include "_title.html.tpl">
  <#include "_description.html.tpl">
  <#include "_notCurrent.html.tpl">
  <div class="blocks">
    <div class="block">
      <#include "_ordering.html.tpl">
    </div>
    <div class="block">
      <#include "_citation.html.tpl">
    </div>
    <div class="block">
      <#include "_spatialExtent.html.tpl">
    </div>
    <div class="block">
      <#include "_browsegraphic.html.tpl">
    </div>
  </div>
  <#include "_additionalInfo.html.tpl">
  <#include "_contacts.html.tpl">
</div>
<script data-main="/static/scripts/main-out" src="/static/vendor/requirejs/require.js"></script>
</@skeleton.master>