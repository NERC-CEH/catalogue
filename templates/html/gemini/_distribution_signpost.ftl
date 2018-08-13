<#if resourceStatus?? && resourceType.value == 'signpost'>
  <div class="panel panel-default hidden-print" id="document-distribution">
    <div class="panel-heading"><p class="panel-title">Get the data</p></div>
    <div class="panel-body">
    <#if resourceType.value == 'signpost' >
      <div class="distribution-signpost">
        <#if onlineResources??>
            <#assign signpostURLs = func.filter(onlineResources, "function", "information")  + func.filter(onlineResources, "function", "search")>
        </#if>
        <#if distributorContacts??>
            <#assign signpostDistributor = func.filter(distributorContacts, "role", "distributor")>
        </#if>

        <b>
            This record describes <#if metadata.catalogue == "eidc">NERC-funded </#if>data managed by 
            <#if signpostDistributor?has_content>
                ${signpostDistributor?first.organisationName}
            <#else>
                a third party
            </#if>
        </b>
        
        <#if signpostURLs?has_content>
            <span class="signpostURL">To access the data visit <a href="${signpostURLs?first.url}">${signpostURLs?first.url}</a></span>
        </#if>
      </div>
    </div>
  </div>
</#if>