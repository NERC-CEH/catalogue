<#if citation?has_content || useConstraints?has_content || accessConstraints?has_content>
  <div class="panel panel-default hidden-print" id="document-reuse">
    <div class="panel-heading"><p class="panel-title">Access and use conditions</p></div>
    <div class="panel-body">
        <#if resourceStatus == "Embargoed">
            <#include "_embargo.html.tpl">
        </#if>
    <#include "_licence.html.tpl">
    <#include "_citation.html.tpl">
    <#include "_otherUseConstraints.html.tpl">
    <#include "_accessConstraints.html.tpl">
    </div>
  </div>
</#if>