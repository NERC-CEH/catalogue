<#assign alertclass="warning" alerticon="exclamation">
<#if errors?size gte 1 || warnings?size gte 1>
    <#if errors?size gte 1 >
        <#assign alertclass="danger" alerticon="exclamation-triangle">
    </#if>

    <div id="qualityAlert">
        <div class="alert alert-${alertclass} alert-dismissible" role="alert">
            <button type="button" class="close" data-dismiss="alert" aria-label="Close" title="dismiss"><span aria-hidden="true">
                <i class="far fa-times-circle"></i>
            </span></button>
            <i class="fas fa-${alerticon}"></i> <b>METADATA QUALITY: </b> There are  
            <#if errors?size gte 1>${errors?size} errors</#if>
            <#if errors?size gte 1 && warnings?size gte 1>and</#if>
            <#if warnings?size gte 1>${warnings?size} warnings</#if>
        </div>
    </div>

</#if>