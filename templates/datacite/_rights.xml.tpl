<#if doc.useConstraints?has_content>
    <#assign licences = func.filter(doc.useConstraints, "code", "license")>
    <#if licences?has_content>
    <rightsList>
        <#list licences as licence>
        <#if licence.code == "license">
        <#if licence.uri?has_content>
            <#if licence.uri="https://www.eidc.ac.uk/licences/OGL/plain">
                <rights rightsURI="${licence.uri}" rightsIdentifier="OGL-UK-3.0" rightsIdentifierScheme="SPDX">${licence.value}</rights>
            <#else>
                <rights rightsURI="${licence.uri}">${licence.value}</rights>
            </#if>
        <#else>
            <rights>${licence.value}</rights>
        </#if>
        </#if>
        </#list>
    </rightsList>
    </#if>
</#if>