<#if licences?has_content>
    dct:rights
    <#list licences as licence>
        <#if licence.uri?has_content><${licence.uri?trim}><#else>"${licence.value}"</#if> <#sep>,
    </#list> ;
</#if>
