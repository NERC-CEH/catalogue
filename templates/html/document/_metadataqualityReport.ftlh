<#macro listItems things>
    <ul>
        <#list things as thing>
            <li>${thing.test}<#sep><br></#sep></li>
        </#list>
    </ul>
</#macro>

<#macro listItems2 things class="" icon="" title="" subtitle="">
    <div class="${class}">
        <p>
            <i class="fa-solid fa-2x ${icon}" > </i> ${title} <small>${subtitle}</small>
        </p>
        <ul>
            <#list things as thing>
                <li>${thing.test}<#sep><br></#sep></li>
            </#list>
        </ul>
    </div>       
</#macro>

<div id="section-metadataQuality" class="clearfix hidden-print">
    <h2>Metadata quality</h2>
        <#if problems?? && problems?has_content>
            <div class="metadataQualityReport">

                <#if errors?? && errors?has_content>
                    <@listItems2 errors "errors" "fa-exclamation-triangle" "ERRORS" "These should always be fixed"/>
                </#if>
                <#if warnings?? && warnings?has_content>
                    <@listItems2 warnings "warnings" "fa-circle-exclamation" "WARNINGS" "These are usually problems that need fixing.  However, they MAY be acceptable in some circumstances - please check carefully"/>
                </#if>
                <#if infos?? && infos?has_content>
                    <@listItems2 infos "info" "fa-circle-info" "OTHER ISSUES" "For absolute best practice, these issues should be addressed"/>
                </#if>
            </div>
    
        <#else>
            <p class="text-success"><i class="fa-solid fa-circle-check"></i> No metadata issues found</p>
        </#if>
</div>
