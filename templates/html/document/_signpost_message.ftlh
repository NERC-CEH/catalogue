<#if distributorContacts??>
    <#assign signpostDistributor = func.filter(distributorContacts, "role", "distributor")>
</#if>

<div class="signpost">
    <div><strong>
        This record describes data managed by 
        <#if signpostDistributor?has_content>
            ${signpostDistributor?first.organisationName}
        <#else>
            a third party
        </#if>
    </strong></div>
</div>