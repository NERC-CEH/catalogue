<#if (resourceType.value)?has_content>
  <tr>
    <th scope="row">Resource Type</th>
    <td id="resource-type" property="dc:type">${resourceType.value?cap_first}</td>
  </tr>
</#if>