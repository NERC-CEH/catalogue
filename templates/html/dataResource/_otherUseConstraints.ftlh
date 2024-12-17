<#if otherConstraints?has_content>
  <#list otherConstraints as otherUseConstraint>
    <p class="otherUseConstraint">
    <#if otherUseConstraint.uri?has_content>
      <a href="${otherUseConstraint.uri}">${otherUseConstraint.value}</a>
    <#else>
      ${otherUseConstraint.value}
    </#if>
    </p>
  </#list>
</#if>
