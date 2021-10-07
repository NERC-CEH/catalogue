<#if relatedDataHoldings?? && relatedDataHoldings?has_content>
    <br><p class="relatedDataHoldings">
    <b>Related Data Holdings</b><br>
    <#list relatedDataHoldings as dataHolding>
        <p>Title: ${dataHolding.title}</p>
        <p>URL: ${dataHolding.url}</p><br>
    </#list>
</p>
</#if>