<#if useConstraints?has_content>
  <div class="useConstraints">
  <#list useConstraints as useConstraint>
    <p>
    <#if useConstraint.uri?has_content>
      <a href="${useConstraint.uri}">${useConstraint.value!'Use Constraint'}</a>
    <#-- starts_with shortened to catch 'reuse' and 're-use' in the wild -->
    <#elseif !useConstraint.value?starts_with("If you re")>
      ${useConstraint.value?html}
    </#if>
    </p>
  </#list>
  </div>
</#if>
