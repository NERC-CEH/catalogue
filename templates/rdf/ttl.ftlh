<#compress>
<#-- Keywords -->
    <#assign allKeywords = []>
    <#if descriptiveKeywords?has_content>
        <#list descriptiveKeywords as descriptiveKeyword>
            <#assign allKeywords = allKeywords + descriptiveKeyword.keywords >
        </#list>
    </#if>
    <#if keywords?has_content>
    <#list keywords as keyword>
        <#assign allKeywords = allKeywords + [keyword]>
    </#list>
    </#if>
<#-- END Keywords -->

<#include "_macros.ftlh">
<#include "_prefixes.ftlh">
@base <${uri?replace(id,"")?replace("id/","")}> .
@prefix : <${uri?replace(id,"")}> .

<#include "_body.ftlh">

</#compress>