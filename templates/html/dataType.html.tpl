<#import "skeleton.html.tpl" as skeleton>
<#import "new-form.html.tpl" as form>
<#import "blocks.html.tpl" as b>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)><#escape x as x?html>
  <@b.metadataContainer "ceh-model">
    <@b.sectionHeading>Data Type</@b.sectionHeading>
    <#if title?? && title?has_content>
      <@b.key "Title" "">${title}</@b.key>
    </#if>
    <#if id?? && id?has_content>
      <@b.key "Id" "">
        <span id="dataTypeId">${id}</span><span class="clipboard-copy pull-right" data-selector="#dataTypeId"></span>
      </@b.key>
    </#if>
    <#if description?? && description?has_content>
      <@b.key "Description" "">${description}</@b.key>
    </#if>
    <#if schema?? && (schema.id?? || schema.fieldName??)>
       <@b.sectionHeading>Schema</@b.sectionHeading>
       <#if schema.id?? && schema.id?has_content>
        <@b.key "Id" "">${schema.id}</@b.key>
      </#if>
      <#if schema.fieldName?? && schema.fieldName?has_content>
        <@b.key "Field Name" "">${schema.fieldName}</@b.key>
      </#if>
      <#if schema.title?? && schema.title?has_content>
        <@b.key "Title" "">${schema.title}</@b.key>
      </#if>
      <#if schema.format?? && schema.format?has_content>
        <@b.key "Format" "">${schema.format}</@b.key>
      </#if>
      <#if schema.fieldDescription?? && schema.fieldDescription?has_content>
        <@b.key "Field Description" "">${schema.fieldDescription}</@b.key>
      </#if>
      <#if schema.missingValues?? && schema.missingValues?has_content>
        <@b.key "Missing Values" "">${schema.missingValues}</@b.key>
      </#if>
      <#if schema.primaryKey?? && schema.primaryKey?has_content>
        <@b.key "Primary Key" "">${schema.primaryKey}</@b.key>
      </#if>
      <#if schema.constraints?? && schema.constraints?has_content>
        <@b.key "Constraints" "">${schema.constraints}</@b.key>
      </#if>
      <#if schema.measurementUnits?? && schema.measurementUnits?has_content>
        <@b.key "Measurement Units" "">${schema.measurementUnits}</@b.key>
      </#if>
    </#if>
     <#if provenance?? && (provenance.creationDate?? || provenance.modificationDate??)>
       <@b.sectionHeading>Provenance</@b.sectionHeading>
       <#if provenance.creationDate?? && provenance.creationDate?has_content>
        <@b.key "Created" "">${provenance.creationDate}</@b.key>
      </#if>
      <#if provenance.modificationDate?? && provenance.modificationDate?has_content>
        <@b.key "Modified" "">${provenance.modificationDate}</@b.key>
      </#if>
      <#if provenance.contributors?? && provenance.contributors?has_content>
        <@b.key "Contributors" "">${provenance.contributors?join(", ")}</@b.key>
      </#if>
    </#if>
    <@b.admin />
  </@b.metadataContainer>
</#escape></@skeleton.master>