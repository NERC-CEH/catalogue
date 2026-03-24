<#import "../skeleton.ftlh" as skeleton>
<#import "../blocks.ftlh" as b>
<#import "../../functions.ftlh" as func>

<#assign
    MD_checks = metadataQuality.check(id)
    problems = MD_checks.getProblems()
    errors = MD_checks.getErrors()
    infos =  MD_checks.getInfo()
    warnings = MD_checks.getWarnings()
    errorList = MD_checks.getErrorList()
    infoList = MD_checks.getInfoList()
    warningList = MD_checks.getWarningList()
    totalIssues = errors + infos +  warnings
>

<#macro base>
  <@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue) footer=true>
    <div id="metadata" class="metadata metadata-monitoring ${metadata.state?lower_case} <#if operationalStatus?? && operationalStatus == "Inactive">inactive</#if>">
    <#if !type??>
    <#else>
      <section class="section">
        <div class="container">
          <@admin />

          <#if operationalStatus?? && operationalStatus == "Inactive">
            <div class="p-3 mb-5 bg-danger-subtle border border-2 border-danger-subtle rounded-3">
              <i class="text-danger fa-solid fa-circle-info fa-xl mx-2"></i>
              <b>This ${(facilityType.value)!codes.lookup('metadata.resourceType', type)?lower_case!''} is inactive</b>
            </div>
          </#if>

          <#if title?? && title?has_content>
            <h1 class="my-4">
              <div class="fs-5">
                <#if (metadata.state == 'draft' || metadata.state == 'pending') >
                  <b class="text-danger">${codes.lookup('publication.state', metadata.state)?upper_case!''}</b>
                </#if>
                <span class="text-body-tertiary">
                  ${codes.lookup('metadata.resourceType', type)!''}
                </span>
              </div>
              <div>
                ${title}
                <#if operationalStatus?? && operationalStatus == "Inactive">
                  <span class="fw-bolder px-2 py-1 ms-3 text-bg-danger fs-6 text-uppercase">inactive</span>
                </#if>
              </div>

              <@alternativeNames />
            </h1>
          </#if>

          <#if description?? && description?has_content>
            <div class="my-4"><@b.linebreaksAndLinks description /></div>
          </#if>

        </div>
      </section>
      <section class="section">
        <div class="container">
          <#nested>

          <#if environmentalDomain??>
           <@keyValue "Environmental domain" ""><@displayKeywords environmentalDomain ", "/></@keyValue>
          </#if>

          <#if purposeOfCollection??>
             <@keyValue "Purpose of collection" ""><@displayKeywords purposeOfCollection/></@keyValue>
          </#if>


          <#if objectives?? && objectives?has_content>
             <@keyValue "Objectives" ""><@b.linebreaksAndLinks objectives /></@keyValue>
          </#if>

          <#if operatingPeriod?has_content>
            <@keyValue "Operating period" "">
              <#list operatingPeriod>
                <#items as op>
                  <@displayPeriod op /><#sep><br></#sep>
                </#items>
               </#list>
            </@keyValue>
          </#if>

          <#if pointsOfContact?has_content || partners?has_content>
            <div class="contacts">
              <h2>Contacts</h2>
              <#if pointsOfContact?has_content>
                <div class="contacts-main">
                  <#list pointsOfContact>
                    <#items as contact>
                      <@contactDetails contact/>
                    </#items>
                  </#list>
                </div>
              </#if>

              <#if partners?has_content>
                <#list partners>
                  <#items as contact>
                    <@contactDetails contact/>
                  </#items>
                </#list>
              </#if>
            </div>
          </#if>


          <#if linksData?has_content>
            <@displayLinks linksData "Links to data"/>
          </#if>

          <#if linksOther?has_content>
            <@displayLinks linksOther "Other links"/>
          </#if>


          <#if (additionalInfo?? && additionalInfo?has_content) || (keywordsParameters?? && keywordsParameters?has_content) || (keywords?? && keywords?has_content)>
            <div class="pb-5">
              <h2>Other metadata</h2>

              <#if additionalInfo?? && additionalInfo?has_content>
                <#list additionalInfo as theInfo>
                  <@keyValue theInfo.key?cap_first "">${theInfo.value}</@keyValue>
                </#list>
              </#if>

              <#if keywordsParameters?? && keywordsParameters?has_content>
                <@keyValue "Parameters measured" ""><@displayKeywords keywordsParameters ","/></@keyValue>
              </#if>

              <#if keywords?? && keywords?has_content>
                <@keyValue "Keywords" ""><@displayKeywords keywords ","/></@keyValue>
              </#if>
            </div>
          </#if>

          <#if permission.userCanEditRestrictedFields(metadata.catalogue)>
            <#include "../dataResource/_metadataqualityReport.ftlh">
          </#if>

        </div>
      </section>

    </#if>
    </div>

  </@skeleton.master>
</#macro>

<#macro admin>
  <#if permission.userCanEdit(id)>

    <#assign
      MD_checks = metadataQuality.check(id)
      problems = MD_checks.getProblems()
      errors = MD_checks.getErrors()
      infos =  MD_checks.getInfo()
      warnings = MD_checks.getWarnings()
      totalIssues = errors + infos +  warnings
      metadataClass="warning"
    >

    <#if totalIssues?? && totalIssues gte 1>
      <#if errors gte 1 >
          <#assign metadataClass="danger">
      <#elseif warnings gte 1 >
          <#assign metadataClass="warning" >
      <#elseif infos gte 1 >
          <#assign metadataClass="primary">
      </#if>
    </#if>

    <div class="row d-print-none" id="adminPanel">
      <div class="text-end adminToolbar" role="toolbar">
        <@editButton />
      </div>
    </div>

    </#if>
</#macro>

<#macro editButton>
  <div class="btn-group btn-group-sm position-relative">
    <button type="button" class="btn btn-outline-secondary btn-wide edit-control" data-document-type="${metadata.documentType}">
      Edit
      <#if totalIssues gte 1>
        <span class="position-absolute top-0 start-0 translate-middle badge rounded-pill bg-${metadataClass}"
        data-bs-toggle="tooltip" data-bs-placement="right" title="Metadata issues">
          ${totalIssues}
          <span class="visually-hidden">Metadata issues</span>
        </span>
      </#if>
    </button>
    <button type="button" class="btn btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown" aria-haspopup="true" aria-expanded="false" >
      <span class="caret"></span>
      <span class="visually-hidden">Toggle Dropdown</span>
    </button>
    <ul class="dropdown-menu dropdown-menu-end">
      <#if totalIssues gte 1>
        <@menuLink "#segment-metadataQuality">
          <span class="badge rounded-pill bg-${metadataClass}">${totalIssues}</span>
          <span class="text-${metadataClass}">metadata issue<#if totalIssues gt 1>s</#if></span>
        </@menuLink>
        <li class="dropdown-divider"></li>
      </#if>

      <@menuLink "/documents/${id}/permission" "fa-solid fa-users">Permissions</@menuLink>
      <@menuLink "/documents/${id}/publication" "fa-solid fa-eye">Publication status</@menuLink>

      <span class="catalogue-control">
        <li class="dropdown-divider"></li>
        <@menuLink "/documents/${id}/catalogue" "fa-solid fa-right-from-bracket">Move to a different catalogue</@menuLink>
      </span>
    </ul>
  </div>
</#macro>

<#macro menuLink link  target="_self">
  <li><a class="dropdown-item" href="${link}" target="${target}" rel="noopener noreferrer">
     <#nested>
  </a></li>
</#macro>

<#macro keyValue key description="" auto_esc=false>
  <div class="row key-value" title="${description}">
    <div class="col-sm-3 key">
     ${key}
    </div>
    <div class="col-sm-9 value">
      <#nested>
    </div>
  </div>
</#macro>

<#macro supplemental supplementalList>
  <#list supplementalList>
    <#items as sup>
      <#if sup.function?has_content?has_content>
        <#assign key = sup.function?capitalize>
      <#else>
          <#assign key = "Other">
      </#if>
      <@keyValue "${key}"><@b.linebreaksAndLinks sup.description /></@keyValue>
    </#items>
  </#list>
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
Operating period
-->
<#macro displayPeriod period>
  <#if period.begin?? && period.begin?has_content>
    ${period.begin}
  <#else>
    …
  </#if>
  to
  <#if period.end?? && period.end?has_content>
    ${period.end}
  <#else>
    …
  </#if>
</#macro>

<#--
Relationships
-->
<#macro relationships relation title="">
  <#local links=jena.relationships(uri, relation) />
  <#if links?has_content>
    <@keyValue title "">
      <#list links as link>
        <@geomUrl link />
      </#list>
    </@keyValue>
  </#if>
</#macro>

<#--
Inverse relationships
-->
<#macro inverseRelationships relation title="">
  <#local links=jena.inverseRelationships(uri, relation) />
  <#if links?has_content>
    <#if title?has_content>
      <@keyValue title "">
        <#list links as link>
          <@geomUrl link />
        </#list>
      </@keyValue>
    <#else>
      <#list links as link>
        <@geomUrl link  />
      </#list>
    </#if>
  </#if>
</#macro>

<#--
Symmetrical relationships
-->
<#macro symmetricRelationships relation title="">
  <#local links = jena.inverseRelationships(uri, relation) />
  <#local links = links + jena.relationships(uri, relation) />

  <#if links?has_content>
    <#if title?has_content>
      <@keyValue title "">
        <#list links as link>
          <@b.blockUrl link />
        </#list>
      </@keyValue>
    <#else>
      <#list links as link>
        <@b.blockUrl link />
      </#list>
    </#if>
  </#if>
</#macro>

<#macro geomUrl link >
  <div>
      <#local linkClass = "">
      <#if link.status?has_content>
        <#local linkClass =  "link-" + link.status?lower_case>
      </#if>
      <#if link.associationType=="monitoringFacility" && !link.geometry?has_content>
        <#local linkClass += " nogeom">
      </#if>
      <a class="${linkClass?trim}" href="<#if link.href??>${link.href}<#else>#</#if>">
        <#if link.title??>
          ${link.title}
        <#else>
          link
        </#if>
      </a>
      <#if link.status?has_content && link.status == "Inactive">
        <small class="text-body-tertiary small"> (INACTIVE)</small>
      </#if>
  </div>
</#macro>

<#macro map mapData>
  <div id="studyarea-map" class="studyarea-map">
    <span content="${mapData}" dataType="geoJson"/>
  </div>
</#macro>

<#macro inverseRelationshipCombinedGeometries relation>
  <#local combinedGeometry=jena.inverseRelationshipCombinedGeometries(uri, relation) />
  <#if combinedGeometry?has_content>
    <@map combinedGeometry />
  </#if>
</#macro>

<#macro relationshipCombinedGeometriesWithOwner relation locationConfidential>
  <#local combinedGeometry=jena.relationshipCombinedGeometriesWithOwner(uri, relation, locationConfidential) />
  <#if combinedGeometry?has_content>
    <@map combinedGeometry />
  </#if>
</#macro>

<#macro programmeCombinedGeometries relation>
  <#local combinedGeometry=jena.programmeCombinedGeometries(uri) />
  <#if combinedGeometry?has_content>
    <@map combinedGeometry />
  </#if>
</#macro>

<#macro contactDetails contact>
  <div class="contact row">
      <#if contact.role?has_content>
      <div class="col-md-3 col-lg-2 col-12">
        <b>${contact.roleDisplayName?cap_first}</b>
      </div>
      </#if>

    <div class="col-md-9 col-lg-10 col-12">
      <#if contact.fullName?has_content>
        <span>
          ${contact.fullName}
          <#if contact.nameIdentifier?has_content>
            <a href="${contact.nameIdentifier}" target="_blank" rel="noopener noreferrer">
              <img src="/static/img/orcid_16x16.png" alt="ORCID iD icon" title="ORCID iD">
            </a>
          </#if>
        </span>
        <br>
      </#if>

      <#if contact.organisationName?has_content>
        <span>
          ${contact.organisationName}
          <#if contact.organisationIdentifier?has_content>
            <a href="${contact.organisationIdentifier}" target="_blank" rel="noopener noreferrer">
              <img src="/static/img/ror_16x16.png" alt="RoR icon" title="Research Organisation Identifier">
            </a>
          </#if>
        </span>
        <br>
      </#if>

      <#if contact.email?has_content>
        <span>
          <a href="mailto:${contact.email}">${contact.email}</a>
        </span>
      </#if>
    </div>
  </div>
</#macro>

<#macro displayLinks linkList title="">
  <#list linkList>
    <@keyValue "${title}" "">
      <#items as link>
        <#if link.url?has_content>
            <#if link.description?has_content>
              <a href="${link.url}" target="_blank" rel="noopener noreferrer">${link.description}</a>
             <#else>
              <a href="${link.url}" target="_blank" rel="noopener noreferrer">${link.url}</a>
             </#if>
        <#else>
          ${link.description}
        </#if><#sep><br></#sep>
      </#items>
    </@keyValue>
  </#list>
</#macro>

<#macro displayKeywords keywords separator="<br>">
  <#list keywords as keyword>

    <#if keyword.uri?? && keyword.uri?has_content>
        <a href="${keyword.uri}">
          <#if keyword.value?? && keyword.value?has_content>
            ${keyword.value}
          <#else>
              ${keyword.uri}
          </#if>
        </a>
    <#elseif keyword.value?? && keyword.value?has_content>
      ${keyword.value}
    <#else>
     <span class="text-muted">missing</span>
    </#if>

    <#sep>${separator?no_esc}</#sep>
  </#list>
</#macro>

<#macro alternativeNames>
  <#assign altNames=[]>
  <#if alternateTitles?? && alternateTitles?has_content>
    <#list alternateTitles as title>
       <#assign altNames = altNames + [title] />
    </#list>
  </#if>

  <#if resourceIdentifiers?? && resourceIdentifiers?has_content>
    <#list resourceIdentifiers as identifier>
      <#if !identifier.code?matches("^http.+[a-f0-9]{8}-([a-f0-9]{4}-){3}[a-f0-9]{12}$")>
        <#if identifier.codeSpace?has_content>
          <#assign altNames = altNames + [identifier.codeSpace + "/" + identifier.code] />
        <#else>
          <#assign altNames = altNames + [identifier.code] />
        </#if>
      </#if>
    </#list>
  </#if>

  <#if altNames?? && altNames?has_content>
    <div class="altNames">
      Also known as
      <#list altNames as altName>
        <span class="altName">${altName}</span><#sep>or<#t>
      </#list>
    </div>
  </#if>
</#macro>
