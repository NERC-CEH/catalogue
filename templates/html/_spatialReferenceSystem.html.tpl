<#if spatialReferenceSystem?? && spatialReferenceSystem.title?? && spatialReferenceSystem.title?has_content>
  <tr>
    <th scope="row">Spatial Reference System</th>
    <td id="spatial-reference-system">${spatialReferenceSystem.title}</td>
  </tr>
</#if>
