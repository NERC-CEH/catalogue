<#include "_citation.html.tpl">

<#if useLimitations?has_content>
<#list useLimitations as useLimitation>
  <#-- starts_with shortened to catch 'reuse' and 're-use' in the wild -->
  <#if !useLimitation?starts_with("If you re")>
	<p>${useLimitation?html}</p>
  </#if>
</#list>
</#if>