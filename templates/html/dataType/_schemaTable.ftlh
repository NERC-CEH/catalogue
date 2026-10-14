<#-- A data type's schema: a row per field. Its own file so a test can render it alone.
     The field's name is `value` (`name` until fe683df84, 2024-10-25; dri-one #472). -->
<table class="table table-schema">
<thead>
  <tr>
    <th>Field</th>
    <th>Type</th>
    <th>Description</th>
    <th>Constraints</th>
  </tr>
</thead>
<tbody>
<#list schema as schemaItem>
<tr>
  <td nowrap="nowrap">
    <#if schemaItem.value?? && schemaItem.value?has_content>
      ${schemaItem.value}
    </#if>
  </td>
  <td nowrap="nowrap">
    <#if schemaItem.type?? && schemaItem.type?has_content>
        ${schemaItem.type}
        <#if schemaItem.format?? && schemaItem.format?has_content>
          <span class="schema-format">(${schemaItem.format})</span>
        </#if>
    </#if>
  </td>
  <td>
    <#if schemaItem.description?? && schemaItem.description?has_content>
      <span class="schema-description">${schemaItem.description}</span>
    <#else>
      <span class="nodata" />
    </#if>
  </td>
  <td>
    <#if schemaItem.constraints?? && schemaItem.constraints?has_content>
      <span class="schema-constraints">
        <#if schemaItem.constraints.minimum?has_content>Minimum : ${schemaItem.constraints.minimum}<br></#if>
        <#if schemaItem.constraints.maximum?has_content>Maximum : ${schemaItem.constraints.maximum}<br></#if>
        <#if schemaItem.constraints.minLength?has_content>Minimum length : ${schemaItem.constraints.minLength}<br></#if>
        <#if schemaItem.constraints.maxLength?has_content>Maximum length : ${schemaItem.constraints.maxLength}<br></#if>
        <#if schemaItem.constraints.unique=true><small class="fa-regular fa-circle-check text-success"></small> Is unique</#if>
      </span>
    <#else>
      <span class="nodata" />
    </#if>
  </td>
</tr>
</#list>
</tbody></table>
