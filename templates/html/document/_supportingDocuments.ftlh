<#if supportingDocumentNames?? && supportingDocumentNames?has_content>
    <br><small class="supportingDocumentNames">
    <#list supportingDocumentNames as name>
        (${name})<#sep><br></#sep>
    </#list>
</small>
</#if>