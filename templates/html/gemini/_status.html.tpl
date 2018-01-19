<#if resourceStatus?has_content && (resourceStatus != "Current")>
    <div id="resourceStatus" class="alert alert-${resourceStatus}" role="alert">
        <#if resourceStatus == "Superseded" || resourceStatus == "Withdrawn">
            <p>
            <i class="fa fa-exclamation-circle fa-lg"></i> <b>THIS ${resourceType.value?upper_case} HAS BEEN WITHDRAWN</b>
                <#if revised??>
                and superseded by <a href="${revised.href}">${revised.title}</a>
                </#if>
            .</p>
            <#if erratum??>
            <p class="errataDescription">${erratum?html?replace("\n", "<br>")}</p>
            </#if>
        <#elseif resourceStatus == "Retired" && parent?has_content>
          <p>See other resources in this series: <a href="${parent.href?html}">${parent.title?html}</a></p>
        <#elseif resourceStatus == "Embargoed">
           <#include "_embargo.html.tpl">
        </#if>
    </div>
<#else>
    <div class="titleSpacer"/>
</#if>
