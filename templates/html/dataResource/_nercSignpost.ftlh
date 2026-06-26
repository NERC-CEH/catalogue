<#if onlineResources??>
    <#assign signpostURLs = information + search>
</#if>

<#if distributors??>
    <#assign distributorName = distributors?first.organisationName >
<#else>
    <#assign distributorName = "a third party" >
</#if>


<div>
    <div>
        This record describes NERC-funded data managed by ${distributorName}
    </div>

    <#if signpostURLs?has_content>
        <div class="signpost-url">
            To access the data visit <a href="${signpostURLs?first.url}">${signpostURLs?first.url}</a>
        </div>
    </#if>
</div>
