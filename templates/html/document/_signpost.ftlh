<#if onlineResources??>
    <#assign signpostURLs = func.filter(onlineResources, "function", "browseGraphic", true)>
</#if>

<#if distributorContacts??>
    <#assign signpostDistributor = func.filter(distributorContacts, "role", "distributor")>
    <#assign distributorName = signpostDistributor?first.organisationName >
<#else>
    <#assign distributorName = "a third party" >
</#if>

<div>This record describes data managed by ${distributorName}</div>

<#include "_licence.ftlh">

<#if signpostURLs?has_content>
    <div class="signpostURL">
        To access the data visit <a href="${signpostURLs?first.url}">${signpostURLs?first.url}</a>
    </div>
</#if>
