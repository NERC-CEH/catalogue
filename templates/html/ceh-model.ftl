<#import "skeleton.ftl" as skeleton>
<#import "blocks.ftl" as b>
<#import "../functions.tpl" as func>

<!-- MACROS
<#macro key key alt="" auto_esc=false>
  <@b.basicRow "key-value">
    <@keyContent key alt>
      <#nested>
    </@keyContent>
  </@b.basicRow>
</#macro>

<#macro keyContent key alt>
  <div class="col-sm-3 key">
    <div class="key-name" title="${alt}">
      ${key}
    </div>
  </div>
  <div class="col-sm-9 value">
    <#nested>
  </div>
</#macro>

<#macro Url value newWindow=false>
  <#if value?matches("^http(s)?://.*")>
    <#if newWindow==true>
      <a href="${value}" target="_blank" rel="noopener noreferrer">${value}</a>
    <#else>
      <a href="${value}">${value}</a>
    </#if>
  </#if>
</#macro>

<#macro commaList values>
  <#list values>
    <#items as value>
      ${value}<sep>, </sep>
    </#items>
  </#list>
</#macro>
-->
<#if responsibleParties?? && responsibleParties?has_content>
  <#assign
    SRO = func.filter(responsibleParties, "role", "owner")
    otherContacts = func.filter(responsibleParties, "role", "owner", true)
  >
</#if>
<#if onlineResources?? && onlineResources?has_content>
  <#assign
    image = func.filter(onlineResources, "function", "browseGraphic")
    otherLinks = func.filter(onlineResources, "function", "browseGraphic", true)
  >
</#if>
<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)><#escape x as x?html>

  <@b.metadataContainer "ceh-model">

    <#if permission.userCanEdit(id)>
    <div class="row hidden-print" id="adminPanel">
        <div class="text-right" id="adminToolbar" role="toolbar">
          <div class="btn-group btn-group-sm">
            <button type="button" class="btn btn-default btn-wide edit-control"  data-document-type="${metadata.documentType}">Edit</button>
            <#if permission.userCanEditRestrictedFields(metadata.catalogue)>
            <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
              <span class="caret"></span>
              <span class="sr-only">Toggle Dropdown</span>
            </button>
            <ul class="dropdown-menu dropdown-menu-right">
              <li><a href="/documents/${id?html}/permission"><i class="fas fa-users"></i> Permissions</a></li>
              <li><a href="/documents/${id?html}/publication"><i class="fas fa-eye"></i> Publication status</a></li>
              <li role="separator" class="divider"></li>
              <li><a href="/documents/${id?html}/catalogue" class="catalogue-control"><i class="fas fa-sign-out-alt"></i> Move to a different catalogue</a></li>
            </ul>
            </#if>
          </div>
        </div>
    </div>
    </#if>

      <#if title?? && title?has_content>
        <h1>${title}</h1>
      </#if>

      <#if description?? && description?has_content>
        <#noescape>
          <div class="description clearfix">
            <#if image?? && image?has_content>
              <#if image?first.url?matches("^http(|s)://.+(jpg|jpeg|gif|png)$")>
                <img src="${image?first.url}" alt="${image?first.name}" class="browseGraphic" />
              </#if>
            </#if>
            <@b.linebreaks description />
          </div>
        </#noescape>
      </#if>

      <#if primaryPurpose?? || SRO??|| otherContacts?? || licenseType?? || onlineResources??>
      <section>
        <#if primaryPurpose?? && primaryPurpose?has_content>
          <@key "Primary purpose">
            <#noescape>
              <@b.linebreaks primaryPurpose />
            </#noescape>
          </@key>
        </#if>

        <#if otherLinks?? && otherLinks?has_content>
          <@key "Web links">
            <#list otherLinks as otherLink>
              <p>
                <#if otherLink.function?? && otherLink.function?has_content>
                  ${otherLink.function?cap_first}: 
                </#if>
                <#if otherLink.url?? && otherLink.url?has_content>
                  <@Url otherLink.url true/>
                </#if>
              </p>
            </#list>
          </@key>
        </#if>

        <#noescape>
        <#if SRO?? && SRO?has_content>
          <@key "Senior responsible officer">
            <#list SRO as SRO>
            ${func.displayContact(SRO, true, true, false)}
            </#list>
          </@key>
        </#if>
        <#if otherContacts?? && otherContacts?has_content>
          <@key "Other contacts">
          <#list otherContacts as otherContact>
            <dt>${otherContact.roleDisplayName?html}</dt>
            <dd>
              <div class="responsibleParty">      
                ${func.displayContact(otherContact, true, true, false)}
              </div>
            </dd>
            </#list>
          </@key>
        </#if>
        </#noescape>

        <#if licenseType?? && licenseType?has_content>
          <@key "License" "License type (open or non-open) under which the model is distributed">${licenseType?cap_first}</@key>
        </#if>
      </section>
      </#if>

      <#if keyInputVariables?? || keyOutputVariables?? || description?? || modelType?? || currentModelVersion?? || modelCalibration??>
      <section>
        <h2>Model Description</h2>
        <#if keyInputVariables?? && keyInputVariables?has_content>
          <@key "Key input variables" "Short phrases to describe basic types of model inputs"><@commaList keyInputVariables /></@key>
        </#if>
        <#if keyOutputVariables?? && keyOutputVariables?has_content>
          <@key "Key output variables" "Short phrases to describe basic types of model outputs"><@commaList keyOutputVariables /></@key>
        </#if>
        <#if modelType?? && modelType?has_content>
          <@key "Model type" "Type which best fits model">${modelType?cap_first}</@key>
        </#if>
        <#if currentModelVersion?? && currentModelVersion?has_content>
          <@key "Current model version" "Most recent release version">
            ${currentModelVersion}
            <#if releaseDate?? && releaseDate?has_content>
            (${releaseDate})
            </#if>
          </@key>
        </#if>
        <#if modelCalibration?? && modelCalibration?has_content>
          <@key "Model calibration" "Does the model need calibration before running? If so, what needs to be supplied to do this?">${modelCalibration}</@key>
        </#if>
      </section>
      </#if>

    <#if spatialDomain?? || spatialResolution?? || temporalResolutionMin?? || temporalResolutionMax??>
    <section>
      <h2>Spatio-Temporal Information</h2>
      <#if spatialDomain?? && spatialDomain?has_content>
        <@key "Spatial domain" "Is the model only applicable to certain areas?">${spatialDomain}</@key>
      </#if>
      <#if spatialResolution?? && spatialResolution?has_content>
        <@key "Spatial resolution" "Spatial resolution at which model works or at which model outputs are generated">${spatialResolution}</@key>
      </#if>
      <#if temporalResolutionMin?? && temporalResolutionMin?has_content>
        <@key "Temporal resolution (min)" "Minimum time step supported by the model">${temporalResolutionMin}</@key>
      </#if>
      <#if temporalResolutionMax?? && temporalResolutionMax?has_content>
        <@key "Temporal resolution (max)" "Maximum time step supported by the model ">${temporalResolutionMax}</@key>
      </#if>
    </section>
    </#if>

    <#if language?? || compiler?? || operatingSystem?? || systemMemory?? || documentation??>
    <section>
      <h2>Technical Information</h2>
      <#if language?? && language?has_content>
        <@key "Language" "Computing language in which the model is written">${language}</@key>
      </#if>
      <#if compiler?? && compiler?has_content>
        <@key "Compiler" "Compiled required">${compiler}</@key>
      </#if>
      <#if operatingSystem?? && operatingSystem?has_content>
        <@key "Operating system" "Operating system typically used to run the model">${operatingSystem}</@key>
      </#if>
      <#if systemMemory?? && systemMemory?has_content>
        <@key "System memory" "Memory required to run code">${systemMemory}</@key>
      </#if>
      <#if documentation?? && documentation?has_content>
        <@key "Documentation" "Location of technical documentation for the model"><@bareUrl documentation /></@key>
      </#if>
    </section>
    </#if>

    <section>
      <h2>QA Information</h2>
      <@key "Developer testing" "Use of a range of developer tools including parallel build and analytical review or sense check"><@b.qa developerTesting /></@key>
      <@key "Internal peer review" "Obtaining a critical evaluation from a third party independent of the development of the model but from within the same organisation"><@b.qa internalPeerReview /></@key>
      <@key "External peer review" "Formal or informationrmal engagement of a third party to conduct critical evaluation from outside the organisation in which the model is being developed"><@b.qa externalPeerReview /></@key>
      <@key "Internal model audit" "Formal audit of a model within the organisation, perhaps involving use of internal audit functions"><@b.qa internalModelAudit /></@key>
      <@key "External model audit" "Formal engagement of external professional to conduct a critical evaluation of the model, perhaps involving audit professionals;"><@b.qa externalModelAudit /></@key>
      <@key "Quality assurance guidelines and checklists" "Model development refers to department’s guidance or other documented QA processes (e.g. third party publications)"><@b.qa qaGuidelinesAndChecklists /></@key>
      <@key "Governance" "At least one of planning, design and/or sign-off of model for use is referred to a more senior person.  There is a clear line of accountability for the model"><@b.qa governance /></@key>
      <@key "Transparency" "Model is placed in the wider domain for scrutiny, and/or results are published"><@b.qa transparency /></@key>
      <@key "Periodic review" "Model is reviewed at intervals to ensure it remains fit for the intended purpose, if used on an ongoing basis"><@b.qa periodicReview /></@key>
      <#if versionHistories?? && versionHistories?has_content>
        <h2>Version control change notes</h2>
        <#list versionHistories as history>
          <@b.versionHistory history /> 
        </#list>
      </#if>
      <#assign modelApplications=jena.modelApplications(uri)/>
      <#if projectUsages?? && projectUsages?has_content || modelApplications?has_content>
        <h2>Project usage</h2>
        <#if projectUsages?? && projectUsages?has_content>
          <#list projectUsages as usage>
            <@b.projectUsage usage />    
          </#list>
        </#if>
        <#list modelApplications>
          <@key "Model Applications" "Applications of the model">
            <#items as md>
              <@b.blockUrl md/>
            </#items>
          </@key>   
        </#list>
      </#if>
    </section>


    <#if references?? && references?has_content>
    <section>
      <h2>References</h2>

      <#list references as ref>
      <@b.repeatRow>
        <#if ref.citation?? && ref.citation?has_content>
          <span class="reference-citation">${ref.citation}</span>
        </#if>
        <#if ref.doi?? && ref.doi?has_content>
          <span class="reference-doi"><@Url ref.doi true/></span>
        </#if>
        <#if ref.nora?? && ref.nora?has_content>
          <span class="reference-nora">${ref.nora}</span>
        </#if>
      </@b.repeatRow>
      </#list>
    </section>
    </#if>
    
    <#if metadataDate?? && metadataDate?has_content>
    <section>
      <h2>Additional metadata</h2>
       
      <#if keywords?? && keywords?has_content>
      <@key "Keywords">
        <#list keywords>
          <#items as keyword>
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
              <span class="text-muted">empty</span>
            </#if>
            <#sep>, </#sep>
          </#items>
          </#list>
      </@key>
      </#if>

      <@key "Last updated">${metadataDateTime?datetime.iso?datetime?string['dd MMMM yyyy  HH:mm']}</@key>

    </section>
    </#if>
  </@b.metadataContainer>
</#escape></@skeleton.master>