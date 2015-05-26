<#import "skeleton.html.tpl" as skeleton>

<@skeleton.master title=title>
  <div id="metadata" class="container" prefix="dc: http://purl.org/dc/terms/ dcat: http://www.w3.org/ns/dcat#">
    <#include "metadata/_title.html.tpl">
    <#include "metadata/_description.html.tpl">
  </div>
</@skeleton.master>