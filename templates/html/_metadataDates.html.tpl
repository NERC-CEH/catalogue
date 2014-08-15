<#if datasetReferenceDate.creationDate??>
  <tr>
    <th scope="row">Creation date</th>
    <td>${datasetReferenceDate.creationDate}</td>
  </tr>
</#if>
<#if datasetReferenceDate.publicationDate??>
  <tr>
    <th scope="row">Publication date</th>
    <td>${datasetReferenceDate.publicationDate}</td>
  </tr>
</#if>
<#if datasetReferenceDate.revisionDate??>
  <tr>
    <th scope="row">Revision date</th>
    <td>${datasetReferenceDate.revisionDate}</td>
  </tr>
</#if>