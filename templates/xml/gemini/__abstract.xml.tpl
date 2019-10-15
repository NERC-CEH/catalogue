<#assign statusMsg = "" abstract = "">

<#if description?? && description?has_content >
    <#assign abstract = description >
</#if>


<#if resourceStatus?? && resourceStatus?has_content >
    <#if resourceStatus == "Withdrawn" || resourceStatus == "Superseded" >
        <#assign statusMsg = "THIS " + resourceType.value?upper_case + " HAS BEEN WITHDRAWN">
    <#elseif resourceStatus == "Embargoed" >
        <#assign statusMsg = "This " + resourceType.value + " is embargoed">
        <#if datasetReferenceDate.releasedDate??>
            <#assign statusMsg = statusMsg + " until "+ datasetReferenceDate.releasedDate?date?string.long>
        </#if>
    </#if>
    <#assign abstract = "[" + statusMsg + "]. " + abstract >
</#if>

<#if citation?has_content>
    <#assign abstract = abstract  + "  Full details about this " + resourceType.value + " can be found at " + citation.url >
</#if>


<#escape x as x?xml>
<gmd:abstract>
    <gco:CharacterString>${abstract}</gco:CharacterString>
</gmd:abstract>
</#escape>
