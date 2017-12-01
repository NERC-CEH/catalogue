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
        <#nested>
        <#if keywords?? && keywords?has_content>
          <@b.key "Keywords" ""><@b.keywords keywords/></@b.key>
        </#if>
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