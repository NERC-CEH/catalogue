<#if resourceStatus?? && resourceStatus?has_content && (resourceStatus != "Current")>
    <div id="resourceStatus" class="alert alert-${resourceStatus}" role="alert">
        <#if resourceStatus == "Superseded" || resourceStatus == "Withdrawn">
            <p>
            <i class="fa fa-exclamation-circle fa-lg"></i> <b>THIS ${resourceType.value?upper_case} HAS BEEN WITHDRAWN</b>
                <#if revised??>
                and superseded by <a href="${revised.href}">${revised.title}</a>
                </#if>
            </p>
            <#if reasonChanged??>
            <p class="reasonChangedDescription">${reasonChanged?html?replace("\n", "<br>")}</p>
            </#if>
        <#elseif resourceStatus == "Retired" && parent?has_content>
          <p>See other resources in this series: <a href="${parent.href?html}">${parent.title?html}</a></p>
        <#elseif resourceStatus == "Embargoed">
           <#include "_embargo.ftl">
        </#if>
    </div>
<#else>
    <div class="titleSpacer"/>
</#if>
