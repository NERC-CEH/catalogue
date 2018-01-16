<#if doc.useConstraints?has_content>
    <#assign licences = func.filter(doc.useConstraints, "code", "license")>
    <#if licences?has_content>
    <rightsList>
        <#list licences as licence>
        <#if licence.code == "license">
        <#if licence.uri?has_content>
            <rights rightsURI="${licence.uri}">${licence.value}</rights>
        <#else>
            <rights>${licence.value}</rights>
        </#if>
        </#if>
        </#list>
    </rightsList>
    </#if>
</#if>