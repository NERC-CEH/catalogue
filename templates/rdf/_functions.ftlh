<#import "../functions.ftlh" as func>
<#compress>
<#setting date_format = 'yyyy-MM-dd'>
<#if useConstraints?has_content>
    <#assign licences = func.filter(useConstraints, "code", "license")>
</#if>
<#if responsibleParties?has_content>
    <#assign
    authors = func.filter(responsibleParties, "role", "author")
    pointsOfContact = func.filter(responsibleParties, "role", "pointOfContact")
    publishers  = func.filter(responsibleParties, "role", "publisher")
    >
</#if>
</#compress>