<#import "../skeleton.html.tpl" as skeleton>
<#import "../blocks.html.tpl" as b>
<#--
OSDP base template
-->
<#macro base>
  <@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)><#escape x as x?html>
      <@b.metadataContainer "ceh-model">
        <div class="row"><@b.admin /></div>
        <#if title?? && title?has_content>
          <@b.key "Title" "Name">${title}</@b.key>
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
      </@b.metadataContainer>
  </#escape></@skeleton.master>
</#macro>

<#--
OSDP Research Artifact base template
-->
<#macro researchArtifact>
  <@base>
    <#if access?? && access?has_content>
      <@b.key "Access" "Access to dataset"><@b.titleUrl access /></@b.key>
    </#if>
    <#if referenceDate?? && referenceDate?has_content>
      <@b.key "Reference Date" "Creation, publication & revision dates"><@refDate referenceDate/></@b.key>
    </#if>
    <#if temporalExtent?? && temporalExtent?has_content>
      <@b.key "Temporal Extent" "Temporal extent of artifact">
          <@o.temporalExt temporalExtent /> 
      </@b.key>
    </#if>
    <#nested>
    <@o.relationships "Related" "Relation between artifacts" "http://onto.nerc.ac.uk/CEHMD/rels/related" />
    <@o.relationships "Supercedes" "Supercedes this artifacts" "http://onto.nerc.ac.uk/CEHMD/rels/supercedes" />
    <@o.relationships "Cites" "Citations for this artifact" "http://onto.nerc.ac.uk/CEHMD/rels/cites" />
    <@o.relationships "Revises" "Artifact revisions" "http://onto.nerc.ac.uk/CEHMD/rels/revises" />
    <@o.relationships "Uses" "Artifact uses" "http://onto.nerc.ac.uk/CEHMD/rels/uses" />
    <@o.relationships "Produces" "Artifact produces" "http://onto.nerc.ac.uk/CEHMD/rels/produces" />
  </@base>
</#macro>

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

<#--
ReferenceDates
-->
<#macro refDate dates>
  <@b.repeatRow>
    <#if dates.creationDate?? && dates.creationDate?has_content>
      <@b.basicRow>
        <@b.keyContent "Creation Date" "Artifact creation date">${dates.creationDate}</@b.keyContent>
      </@b.basicRow>
    </#if>
    <#if dates.publicationDate?? && dates.publicationDate?has_content>
      <@b.basicRow>
        <@b.keyContent "Publication Date" "Artifact publication date">${dates.publicationDate}</@b.keyContent>
      </@b.basicRow>
    </#if>
    <#if dates.revisionDate?? && dates.revisionDate?has_content>
      <@b.basicRow>
        <@b.keyContent "Revision Date" "Artifact revision date">${dates.revisionDate}</@b.keyContent>
      </@b.basicRow>
    </#if>
  </@b.repeatRow>
</#macro>

<#--
ParametersMeasured
-->
<#macro parametersMeasured parameter>
  <@b.repeatRow>
    <#if parameter.name?? && parameter.name?has_content>
      <@b.basicRow>
        <@b.keyContent "Name" "Name of parameter">${parameter.name}</@b.keyContent>
      </@b.basicRow>
    </#if>
    <#if parameter.definition?? && parameter.definition?has_content>
      <@b.basicRow>
        <@b.keyContent "Definition" "Definition of parameter">${parameter.definition}</@b.keyContent>
      </@b.basicRow>
    </#if>
    <#if parameter.unitOfMeasure?? && parameter.unitOfMeasure?has_content>
      <@b.basicRow>
        <@b.keyContent "Unit of measure" "Units of parameter">${parameter.unitOfMeasure}</@b.keyContent>
      </@b.basicRow>
    </#if>
  </@b.repeatRow>
</#macro>

<#--
Temporal Extent
-->
<#macro temporalExt temporalExtent>
  <#if temporalExtent.begin?? && temporalExtent.begin?has_content>
    ${temporalExtent.begin}
  <#else>
    …
  </#if>
  to
  <#if temporalExtent.end?? && temporalExtent.end?has_content>
    ${temporalExtent.end}
  <#else>
    …
  </#if>
</#macro>

<#macro relationships title description relation>
  <#local links=jena.relationships(uri, relation) />
  <#if links?has_content>
    <@b.key title description>
      <#list links as link>
        <@b.blockUrl link /> 
      </#list>
    </@b.key>
  </#if>
</#macro>

<#macro inverseRelationships title description relation>
  <#local links=jena.inverseRelationships(uri, relation) />
  <#if links?has_content>
    <@b.key title description>
      <#list links as link>
        <@b.blockUrl link /> 
      </#list>
    </@b.key>
  </#if>
</#macro>