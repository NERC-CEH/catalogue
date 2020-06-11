<#if otherConstraints?has_content>
  <div class="" id="section-otherUseConstraints">
  <#list otherConstraints as otherUseConstraint>
    <p class="otherUseConstraint">
    <#if otherUseConstraint.uri?has_content>
      <a href="${otherUseConstraint.uri}">${otherUseConstraint.value?html}</a>
    <#else>
      ${otherUseConstraint.value?html}
    </#if>
    </p>
  </#list>
  </div>
</#if>
