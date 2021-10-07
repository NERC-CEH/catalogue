<#if supportingDocumentNames?? && supportingDocumentNames?has_content>
    <br><p class="supportingDocumentNames">
    <b>Supporting Documents</b><br>
    <#list supportingDocumentNames as name>
        ${name}<#sep><br></#sep>
    </#list>
</p>
</#if>