<#include "_citation.html.tpl">

<#if useLimitations?has_content>
  <div class="useLimitations">
  <#list useLimitations as useLimitation>
    <p>
	<#if useLimitation.uri?has_content>
      <a href="${useLimitation.uri}">${useLimitation.value!'Use Limitation'}</a>
    <#elseif !useLimitation.value?starts_with("If you re")>
      ${useLimitation.value?html}
    </#if>
	</p>
  </#list>
  </div>
</#if>