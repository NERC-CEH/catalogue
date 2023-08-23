<#if onlineResources??>
    <#assign signpostURLs = func.filter(onlineResources, "function", "browseGraphic", true)>
</#if>

<#if distributorContacts??>
    <#assign signpostDistributor = func.filter(distributorContacts, "role", "distributor")>
    <#assign distributorName = signpostDistributor?first.organisationName >
<#else>
    <#assign distributorName = "a third party" >
</#if>

<#-- If data came from SITES -->
<#if signpostURLs?has_content && signpostURLs?first.url?matches(".+fieldsites.se\\/.+", "i")> 
    <#assign logosrc ="/static/img/elter/sites_logo.png" >
    <#assign logoalt ="SITES logo" >
    <#assign distributorName = "the Swedish Infrastructure for Ecosystem Science (SITES)" >
</#if>


<div class="signpost-content">
    <#if logosrc?has_content>
        <div class="signpost-logo"><img src="${logosrc}" alt="${logoalt}"></div>
    </#if>

    <div class="signpost-text">This record describes data managed by ${distributorName}</div>

    <#if signpostURLs?has_content>
        <div class="signpost-url">
            To access the data visit <a href="${signpostURLs?first.url}">${signpostURLs?first.url}</a>
        </div>
    </#if>
    
    <div class="signpost-licence">
        <#include "_licence.ftlh">
     </div>
</div>

