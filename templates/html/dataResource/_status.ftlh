<#assign
    latestVersion=jena.latestVersion(uri)
    catalogueContact=catalogue.id?upper_case>
<#if catalogue.id == "ukscape">
    <#assign catalogueContact="UK-SCAPE Programme Office">
</#if>

<#if availability == "Superseded" || availability == "Withdrawn" || availability == "Deleted">
    <div>
        <strong>THIS ${recordType?upper_case} HAS BEEN ${availability?upper_case}</strong>
        <#if latestVersion?has_content>
            The latest version is <a href="${latestVersion?first.href}">${latestVersion?first.title}</a>
        </#if>
    </div>
    <#if reasonChanged??>
        <div><@blocks.linebreaksAndLinks reasonChanged/></div>
    </#if>

    <#if availability == "Superseded" || availability == "Withdrawn">
      <div>If you need access to the archived version, please <a href="${catalogue.contactUrl}" target="_blank" rel="noopener noreferrer">contact the ${catalogueContact}</a></div>
    </#if>

<#elseif availability == "Restricted">
    <span>ACCESS TO THIS ${recordType?upper_case} IS RESTRICTED</span>
    <br>${accessLimitation.description} If you need access, please <a href="${catalogue.contactUrl}" target="_blank" rel="noopener noreferrer">contact ${catalogueContact}</a>
<#elseif availability == "Embargoed">
    <div class="embargoNote">
        <strong>This ${recordType} is under embargo</strong>
        <#if datasetReferenceDate?? && datasetReferenceDate.releasedDate?has_content>
            and will be made available by ${datasetReferenceDate.releasedDate?date?string["d MMMM yyyy"]} at the latest
        </#if>
        &nbsp;<a href="https://www.eidc.ac.uk/help/faq/embargos" target="_blank" rel="noopener noreferrer" title="more information about embargoes"> <small> Find out more &raquo;</small></a>
    </div>

</#if>

