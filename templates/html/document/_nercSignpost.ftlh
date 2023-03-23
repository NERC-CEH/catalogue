<#if onlineResources??>
    <#assign signpostURLs = func.filter(onlineResources, "function", "information")  + func.filter(onlineResources, "function", "search")>
</#if>
<#if distributorContacts??>
    <#assign signpostDistributor = func.filter(distributorContacts, "role", "distributor")>
</#if>

<div>
    This record describes NERC-funded data managed by 
    <#if signpostDistributor?has_content>
        ${signpostDistributor?first.organisationName}
    <#else>
        a third party
    </#if>
</div>

<#if signpostURLs?has_content>
    <div class="signpostURL">
        To access the data visit <a href="${signpostURLs?first.url}">${signpostURLs?first.url}</a>
    </div>
</#if>
