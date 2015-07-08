<#include "_citation.html.tpl">

<#if useLimitations?has_content>
<#list useLimitations as useLimitation>
  <#if useLimitation.uri?has_content>
    <a href="${useLimitation.uri}">${useLimitation.value!'Use Limitation'}</a> 
  <#-- starts_with shortened to catch 'reuse' and 're-use' in the wild -->
  <#elseif !useLimitation.value?starts_with("If you re")>
    <p>${useLimitation.value?html}</p>
  </#if>
</#list>
</#if>