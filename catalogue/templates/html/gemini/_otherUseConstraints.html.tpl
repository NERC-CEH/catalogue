<#if useConstraints?has_content>
  <div class="" id="section-otherUseConstraints">
  <#list useConstraints as otherUseConstraint>
  
	<#if otherUseConstraint.code != 'license'>
	<p class="otherUseConstraint">
    <#if otherUseConstraint.uri?has_content>
      <a href="${otherUseConstraint.uri}">${otherUseConstraint.value?html}</a>
    <#else>
      ${otherUseConstraint.value?html}
    </#if>
    </p>
	</#if>
	
  </#list>
  </div>
</#if>
