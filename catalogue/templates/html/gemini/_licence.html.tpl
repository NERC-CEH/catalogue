<#if useConstraints?has_content>
  <#list useConstraints as licence>
	
	<#if licence.code == 'license'>
	<p class="licenceText">
    <#if licence.uri?has_content>
      <a href="${licence.uri}">${licence.value?html}</a>
    <#else>
      ${licence.value?html}
    </#if>
    </p>
	</#if>

  </#list>
</#if>
