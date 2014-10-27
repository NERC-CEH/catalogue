<#if spatialReferenceSystems?has_content>
  <tr>
    <th scope="row">Spatial Reference System</th>
    <td id="spatial-reference-system">
        <#list spatialReferenceSystems as spatialReferenceSystem>
            <#if spatialReferenceSystem.title?has_content>
                ${spatialReferenceSystem.title}<#if spatialReferenceSystem_has_next>, </#if>
            <#else>
                ${spatialReferenceSystem.reference}<#if spatialReferenceSystem_has_next>, </#if>
            </#if>
        </#list>
    </td>
  </tr>
</#if>
