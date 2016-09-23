<#if citation?has_content || useConstraints?has_content || accessConstraints?has_content>
  <div class="panel panel-default hidden-print" id="document-reuse">
    <div class="panel-heading"><p class="panel-title">Use of this resource is subject to these restrictions</p></div>
    <div class="panel-body">
    <#include "_licence.html.tpl">
    <#include "_citation.html.tpl">
    <#include "_otherUseConstraints.html.tpl">
    <#include "_accessConstraints.html.tpl">
    </div>
  </div>
</#if>