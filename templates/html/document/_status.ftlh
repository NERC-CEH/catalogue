<#assign
    superseded=jena.superseded(uri)
    supersededContact=catalogue.id?upper_case>
<#if catalogue.id == "ukscape">
    <#assign supersededContact="UK-SCAPE Programme Office">
</#if>

<#if resourceStatus == "Superseded" || resourceStatus == "Withdrawn" || resourceStatus == "Deleted">
    <p>
        <strong>THIS ${recordType?upper_case} HAS BEEN ${resourceStatus?upper_case}</strong>
        <#if superseded?has_content>
            The latest version is <a href="${superseded?first.href}">${superseded?first.title}</a>
        </#if>
    </p>
    <#if reasonChanged??>
        <p><@blocks.linebreaksAndLinks reasonChanged/></p>
    </#if>

    <#if resourceStatus == "Superseded" || resourceStatus == "Withdrawn">
      <p>If you need access to this archived version, please <a href="${catalogue.contactUrl}" target="_blank" rel="noopener noreferrer">contact the ${supersededContact}</a></p>
    </#if>

<#elseif resourceStatus == "Restricted">
    <span>ACCESS TO THIS ${recordType?upper_case} IS RESTRICTED</span>
    <br>${accessLimitation.description} If you need access, please <a href="${catalogue.contactUrl}" target="_blank" rel="noopener noreferrer">contact ${supersededContact}</a>
<#elseif resourceStatus == "Embargoed">
    <p>
        <strong>This ${recordType} is under embargo</strong>
        <#if datasetReferenceDate?? && datasetReferenceDate.releasedDate?has_content>
            and will be made available by ${datasetReferenceDate.releasedDate?date?string["d MMMM yyyy"]} at the latest
        </#if>
        &nbsp;<a href="https://www.eidc.ac.uk/help/faq/embargos" target="_blank" rel="noopener noreferrer" title="more information about embargoes"> <small> Find out more &raquo;</small></a>
    </p>
<#elseif resourceStatus == "In-progress">
    <p>
        <strong>This ${recordType} is currently being generated</strong>
        <#if datasetReferenceDate?? && datasetReferenceDate.releasedDate?has_content>
            and is expected to become available by ${datasetReferenceDate.releasedDate?date?string["d MMMM yyyy"]}
        </#if>
    </p>
</#if>

