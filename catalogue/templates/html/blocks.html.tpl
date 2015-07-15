<#--
Generates a title block of a metadata page. Do this by looking up the metadata
type from a code list. Also display a label if this document is currently in
draft or is pending publication
-->
<#macro title title="" type="">
  <div class="visible-print-block text-center"><img src="/static/img/CEHlogoSmall.png"></div>

  <#if title?has_content>
    <h1 id="document-title">
      <#if type?has_content>
        <small id="resource-type">${codes.lookup('metadata.resourceType', type)!''}</small><br>
      </#if>
      ${title?html}
      <#if (metadata.state == 'draft' || metadata.state == 'pending') >
        <small> - ${codes.lookup('publication.state', metadata.state)!''}</small>
      </#if>
    </h1>
  </#if>
</#macro>

<#--
Create a description block, replace any carridge returns with link breaks
-->
<#macro description value>
  <#if value?has_content>
    <p id="document-description">${value?html?replace("\n", "<br>")}</p>
  </#if>
</#macro>
