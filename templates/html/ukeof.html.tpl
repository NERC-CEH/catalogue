<#import "skeleton.html.tpl" as skeleton>

<@skeleton.master title=title>
<div id="metadata" class="container" prefix="dc: http://purl.org/dc/terms/ dcat: http://www.w3.org/ns/dcat#">
  <strong>
    This is a UKEOF Record. It is processed with a separate template to that of the
    Gemini records.
  </strong>

  <#include "_title.html.tpl">
  <#include "_description.html.tpl">
</@skeleton.master>