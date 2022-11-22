<#if onlineResources??>
    <#assign signpostURLs = func.filter(onlineResources, "function", "information")  + func.filter(onlineResources, "function", "search")>
</#if>
<h2>Get the data</h2>
<div class="distribution-signpost">
    <#include "_signpost_message.ftlh">

    <#if signpostURLs?has_content>
        <span class="signpostURL">To access the data visit <a href="${signpostURLs?first.url}">${signpostURLs?first.url}</a></span>
    </#if>

    <#include "_licence.ftlh">
</div>
