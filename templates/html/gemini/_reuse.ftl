<#if citation?has_content || useConstraints?has_content || accessConstraints?has_content>
  <div class="panel panel-default hidden-print" id="document-reuse">
    <div class="panel-heading"><p class="panel-title">Access and use conditions</p></div>
    <div class="panel-body">
        <#if resourceStatus?? && resourceStatus == "Embargoed">
            <#include "_embargo.ftl">
        </#if>
    <#include "_licence.ftl">
    <#include "_citation.ftl">
    <#include "_otherUseConstraints.ftl">
    <#include "_accessConstraints.ftl">
    </div>
  </div>
</#if>