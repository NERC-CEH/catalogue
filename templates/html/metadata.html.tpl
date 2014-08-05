<#import "skeleton.html.tpl" as skeleton>

<@skeleton.master title=title rdfa=true>
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
</@skeleton.master>