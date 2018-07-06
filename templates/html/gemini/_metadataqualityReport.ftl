<div id="section-metadataQuality" class="clearfix">
    <h3>Metadata quality</h3>
    <#if problems??>
    <table class="table table-bordered table-condensed">
        <tbody>
        
        <#list problems as problem>
            <#if problem.severity == "WARNING">
                <#assign rowStyle="warning" iconStyle="fa-exclamation">
            <#else>
                <#assign rowStyle="danger" iconStyle="fa-exclamation-triangle">
            </#if>
            <tr class="${rowStyle}">
                <td class="text-center"><i class="fas ${iconStyle}" /></td>
                <td>${problem.test}</td>
            </tr>
        </#list>
        </tbody>
    </table>
    <#else>
        <p class="text-success"><i class="fas fa-check-circle"></i> No issues found</p>
    </#if>
</div>