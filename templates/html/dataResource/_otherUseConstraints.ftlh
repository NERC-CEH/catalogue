<#if otherConstraints?has_content>
  <#list otherConstraints as otherUseConstraint>
    <div class="otherUseConstraint">
    <#if otherUseConstraint.uri?has_content>
      <a href="${otherUseConstraint.uri}">${otherUseConstraint.value}</a>
    <#else>
      <@blocks.linebreaks otherUseConstraint.value />
    </#if>
    </div>
  </#list>
</#if>
