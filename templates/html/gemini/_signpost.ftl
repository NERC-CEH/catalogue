<#assign signpostURLs = func.filter(onlineResources, "function", "information")  + func.filter(onlineResources, "function", "search")>

<#assign signpostPublisher = func.filter(responsibleParties, "role", "publisher")>


<b>This record describes NERC-funded data 
<#if signpostPublisher?has_content>
    held by ${signpostPublisher?first.organisationName} on behalf of 
<#else>
   not held by the 
</#if>
the Envionmental Information Data Centre. </b> 
<#if signpostURLs?has_content>
    To access the data visit <a href="${signpostURLs?first.url}">${signpostURLs?first.url}</a>
</#if>
