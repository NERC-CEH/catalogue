<p>
    <b>This ${recordType} is embargoed</b> 
    <#if datasetReferenceDate?? && datasetReferenceDate.releasedDate?has_content>
        and will be made available by ${datasetReferenceDate.releasedDate?date?string["d MMMM yyyy"]} at the latest
    </#if>
    &nbsp;<a href="http://eidc.ceh.ac.uk/help/faq/embargos" target="_blank" rel="noopener noreferrer" title="more information about embargos"><i class="fas fa-question-circle text-info"></i></a>
</p>