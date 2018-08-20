<div id="section-metadataQuality" class="clearfix">
    <h3>Metadata quality</h3>
    <#if problems?? && problems?has_content>
    <table class="table table-bordered table-condensed">
        <tbody>
        
        <#list problems as problem>
            <#if problem.severity == "WARNING">
                <#assign rowStyle="warning" iconStyle="fa-exclamation" hovertext="">
            <#elseif problem.severity == "CAUTION">
                <#assign rowStyle="caution" iconStyle="fa-exclamation-triangle" hovertext="This is an error but MAY be acceptable in some circumstances - please check carefully">
            <#else>
                <#assign rowStyle="error" iconStyle="fa-times-circle" hovertext="This is an error and should be corrected">
            </#if>
            
            <tr class="MDquality_${rowStyle}"  title="${hovertext}">
                <td class="text-center"><i class="fas ${iconStyle}" /></td>
                <td>${problem.test}</td>
            </tr>
        </#list>
        </tbody>
    </table>
    <#else>
        <p class="text-success"><i class="fas fa-check-circle"></i> No metadata issues found</p>
    </#if>
</div>