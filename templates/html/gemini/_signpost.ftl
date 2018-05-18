<#if onlineResources??>
    <#assign signpostURLs = func.filter(onlineResources, "function", "information")  + func.filter(onlineResources, "function", "search")>
</#if>
<#if distributorContacts??>
    <#assign signpostDistributor = func.filter(distributorContacts, "role", "distributor")>
</#if>

<b>
    This record describes <#if metadata.catalogue == "eidc">NERC-funded </#if>data managed by 
    <#if signpostDistributor?has_content>
        ${signpostDistributor?first.organisationName}
    <#else>
        a third party
    </#if>
</b>
 
<#if signpostURLs?has_content>
    <span class="signpostURL">To access the data visit <a href="${signpostURLs?first.url}">${signpostURLs?first.url}</a></span>
</#if>