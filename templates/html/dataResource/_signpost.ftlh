<#if onlineResources??>
    <#assign signpostURLs = nonBrowseGraphics>
</#if>

<#if distributors?has_content>
    <#assign distributorName = distributors?first.organisationName >
<#else>
    <#assign distributorName = "a third party" >
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

