<#import "../blocks.ftlh" as b>

<#if resourceStatus == "Superseded" || resourceStatus == "Withdrawn" || resourceStatus == "Embargoed" || resourceStatus == "In-progress">
    <div id="resourceStatus" class="alort alort-${resourceStatus}" role="alert">
        <#if resourceStatus == "Superseded" || resourceStatus == "Withdrawn">
            <div>
                <i class="fas fa-info-circle fa-2x"></i>
            </div>
            <div>
                <p>
                <b>THIS ${recordType?upper_case} HAS BEEN ${resourceStatus?upper_case}.</b>
                    <#assign superseded=jena.superseded(uri) />
                    <#if superseded?has_content>
                        The latest version is <a href="${superseded?first.href}">${superseded?first.title}</a>
                    </#if>
                </p>
                <#if reasonChanged??>
                    <p><@b.linebreaks reasonChanged/></p>
                </#if>
                <#if catalogue.id == "ukscape">
                    <#assign supersededContact="UK-SCAPE Programme Office">
                <#else>
                    <#assign supersededContact=catalogue.id?upper_case>
                </#if>
                <p>If you need access to this archived version, please <a href="${catalogue.contactUrl}" target="_blank" rel="noopener noreferrer">contact the ${supersededContact}</a></p>
            </div>
        <#elseif resourceStatus == "Embargoed">
        <p>
            <b>This ${recordType} is embargoed</b>
            <#if datasetReferenceDate?? && datasetReferenceDate.releasedDate?has_content>
                and will be made available by ${datasetReferenceDate.releasedDate?date?string["d MMMM yyyy"]} at the latest
            </#if>
            &nbsp;<a href="https://www.eidc.ac.uk/help/faq/embargos" target="_blank" rel="noopener noreferrer" title="more information about embargoes"><i class="fas fa-question-circle text-info"></i></a>
        </p>
        <#elseif resourceStatus == "In-progress">
         <p>
             <b>This ${recordType} is currently being generated</b>
             <#if datasetReferenceDate?? && datasetReferenceDate.releasedDate?has_content>
                 and is expected to become available from ${datasetReferenceDate.releasedDate?date?string["d MMMM yyyy"]} at the latest
             </#if>
        </p>
        </#if>
    </div>
<#else>
</#if>
