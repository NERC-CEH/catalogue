<#import "../skeleton.ftlh" as skeleton>
<#import "../blocks.ftlh" as b>
<#--
OSDP base template
-->

<#assign odspScripts>
  <script src="/static/scripts/clipboard-bundle.js"></script>
  <script src="/static/scripts/editor-bundle.js"></script>
</#assign>

<#macro base>
  <@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
      <@b.metadataContainer "ceh-model">
        <div class="row"><@b.admin /></div>
        <#if title?? && title?has_content>
          <@b.key "Title" "Name">${title}</@b.key>
        </#if>
        <#if id?? && id?has_content>
          <@b.key "Id" "Id">${id}<span class="clipboard-copy float-end"></span></@b.key>
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
      </@b.metadataContainer>
  </@skeleton.master>
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
    <@o.relationships "Supersedes" "Supersedes this artifacts" "http://onto.nerc.ac.uk/CEHMD/rels/supersedes" />
    <@o.relationships "Cites" "Citations for this artifact" "http://onto.nerc.ac.uk/CEHMD/rels/cites" />
    <@o.relationships "Revises" "Artifact revisions" "http://onto.nerc.ac.uk/CEHMD/rels/revises" />
    <@o.relationships "Uses" "Artifact uses" "http://onto.nerc.ac.uk/CEHMD/rels/uses" />
    <@o.relationships "Produces" "Artifact produces" "http://onto.nerc.ac.uk/CEHMD/rels/produces" />

    <@o.inverseRelationships "Created By" "Agent who created this artifact" "http://onto.nerc.ac.uk/CEHMD/rels/creates" />
    <@o.inverseRelationships "Related" "Relation between artifacts" "http://onto.nerc.ac.uk/CEHMD/rels/related" />
    <@o.inverseRelationships "Superseded By" "Superseded by this artifacts" "http://onto.nerc.ac.uk/CEHMD/rels/supersedes" />
    <@o.inverseRelationships "Cited By" "Cited by this artifact" "http://onto.nerc.ac.uk/CEHMD/rels/cites" />
    <@o.inverseRelationships "Revised By" "Revised by this artifact" "http://onto.nerc.ac.uk/CEHMD/rels/revises" />
    <@o.inverseRelationships "Used By" "Used by this artifact" "http://onto.nerc.ac.uk/CEHMD/rels/uses" />
    <@o.inverseRelationships "Produced By" "Produced by this artifact" "http://onto.nerc.ac.uk/CEHMD/rels/produces" />
    <@o.inverseRelationships "Owned By" "Owned by this artifact" "http://onto.nerc.ac.uk/CEHMD/rels/owns" />
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
        <@b.keyContent "Name" "Name of parameter"><@b.urlOrString parameter.name /></@b.keyContent>
      </@b.basicRow>
    </#if>
    <#if parameter.unitOfMeasure?? && parameter.unitOfMeasure?has_content>
      <@b.basicRow>
        <@b.keyContent "Unit of measure" "Units of parameter"><@b.urlOrString parameter.unitOfMeasure /></@b.keyContent>
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

<#macro relationships title description relation> <#--UPDATE-->
  <#local links=jena.relationships(uri, relation) />
  <#if links?has_content>
    <@b.key description "">
      <#list links as link>
        <@b.blockUrl link />
      </#list>
    </@b.key>
  </#if>
</#macro>

<#macro inverseRelationships title description relation> <#--UPDATE-->
  <#local links=jena.inverseRelationships(uri, relation) />
  <#if links?has_content>
    <@b.key description  "">
      <#list links as link>
        <@b.blockUrl link />
      </#list>
    </@b.key>
  </#if>
</#macro>

<#macro boundingBox boundingBox>
  <div id="studyarea-map" class="studyarea-map">
      <span content="${boundingBox.bounds}" datatype="geoJson"/>
  </div>
</#macro>

<#macro geometry geometry>
  <div id="geometry-map" class="geometry-map">
      <span content="${geometry}" datatype="geo:wktLiteral"/>
  </div>
</#macro>

<#--
ObservationCapability
-->
<#macro observationCapability oc>
  <@b.repeatRow>
    <#if oc.observingTime?? && oc.observingTime?has_content>
      <@b.basicRow>
        <@b.keyContent "Observing Time" "Period of time spent on observation">${oc.observingTime}</@b.keyContent>
      </@b.basicRow>
    </#if>
    <#if oc.observedPropertyName?? || oc.observedPropertyUnitOfMeasure??>
      <@b.basicRow>
        <@b.keyContent "Observed Property" "Observed Property">
        <#if oc.observedPropertyName?has_content>
          <div>
            <span class="text-muted">Name</span>
            <@b.urlOrString oc.observedPropertyName />
          </div>
        </#if>
        <#if oc.observedPropertyUnitOfMeasure?has_content>
          <div>
            <span class="text-muted">Unit of measure</span>
            <@b.urlOrString oc.observedPropertyUnitOfMeasure />
          </div>
        </#if>
        </@b.keyContent>
      </@b.basicRow>
    </#if>
    <#if oc.procedureName?? && oc.procedureName?has_content>
      <@b.basicRow>
        <@b.keyContent "Procedure Name" "Name of observing procedure">
          <@b.urlOrString oc.procedureName/>
        </@b.keyContent>
      </@b.basicRow>
    </#if>
  </@b.repeatRow>
</#macro>
