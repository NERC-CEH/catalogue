<#if (resourceType.value)?has_content>
  <tr>
    <td>Resource Type</td>
    <td id="resource-type" property="dc:type">${resourceType.value?cap_first}</td>
  </tr>
</#if>