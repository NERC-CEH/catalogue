<#import "../skeleton.html.tpl" as skeleton>
<#import "../blocks.html.tpl" as b>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)><#escape x as x?html>
      <@b.metadataContainer "ceh-model">
        <div class="row"><@b.admin /></div>
        <#if title?? && title?has_content>
          <@b.key "Title" "Name">${title}</@b.key>
        </#if>
        <#if type?? && type?has_content>
          <@b.key "Type" "Type of record">${codes.lookup('metadata.resourceType', type)!''}</@b.key>
        </#if>
        <#if description?? && description?has_content>
          <@b.key "Description" "Description of OSDP"><@b.linebreaks description /></@b.key>
        </#if>
        <#if specimenTypes?? && specimenTypes?has_content>
          <@b.key "Specimen Types" ""><@b.keywords specimenTypes/></@b.key>
        </#if>
        <#include "_extent.html.tpl">
        <#if resourceIdentifiers?? && resourceIdentifiers?has_content>
          <@b.key "Identifiers" "">
            <#list resourceIdentifiers as resourceIdentifier>
              <@identifier resourceIdentifier /> 
            </#list>
          </@b.key>
        </#if>
        <#if archiveType?? && archiveType?has_content>
          ${archiveType}
        </#if>
      </@b.metadataContainer>
  </#escape></@skeleton.master>

  <#--
Identifiers
-->
<#macro identifier ident>
  <@b.repeatRow>
    <#if ident.code?? && ident.code?has_content>
      <@b.basicRow>
        <@b.keyContent "Code" "">${ident.code}</@b.keyContent>
      </@b.basicRow>
    </#if>
    <#if ident.codeSpace?? && ident.codeSpace?has_content>
      <@b.basicRow>
        <@b.keyContent "Codespace" "">${ident.codeSpace}</@b.keyContent>
      </@b.basicRow>
    </#if>
    <#if ident.version?? && ident.version?has_content>
      <@b.basicRow>
        <@b.keyContent "Version" "">${ident.version}</@b.keyContent>
      </@b.basicRow>
    </#if>
  </@b.repeatRow>
</#macro>
