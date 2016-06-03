<#if accessConstraints?has_content>
  <div class="" id="section-accessConstraints">
  <#list accessConstraints as accessConstraint>
  
	<#if accessConstraint.code != 'license'>
	<p class="accessConstraint">
    <#if accessConstraint.uri?has_content>
      <a href="${accessConstraint.uri}">${accessConstraint.value?html}</a>
    <#else>
      ${accessConstraint.value?html}
    </#if>
    </p>
	</#if>
	
  </#list>
  </div>
</#if>
