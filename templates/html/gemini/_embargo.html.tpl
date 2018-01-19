<p>
    <b>This ${resourceType.value} is embargoed</b> 
    <#if datasetReferenceDate.releasedDate?has_content>
        and will be made available by ${datasetReferenceDate.releasedDate?date?string.long} at the latest
    </#if>
    &nbsp;<a href="http://eidc.ceh.ac.uk/help/faq/embargos" target="_blank" title="more information about embargos"><i class="fa fa-question-circle text-info"></i></a>
</p>