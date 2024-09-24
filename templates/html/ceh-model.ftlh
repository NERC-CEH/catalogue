<#import "skeleton.ftlh" as skeleton>
<#import "blocks.ftlh" as b>
<#import "ceh-model-macros.ftlh" as m>
<#import "../functions.ftlh" as func>

<#if responsibleParties?? && responsibleParties?has_content>
  <#assign
    SRO = func.filter(responsibleParties, "role", "owner")
    otherContacts = func.filter(responsibleParties, "role", "owner", true)
  >
</#if>
<#if onlineResources?? && onlineResources?has_content>
  <#assign
    images = func.filter(onlineResources, "function", "browseGraphic")
    otherLinks = func.filter(onlineResources, "function", "browseGraphic", true)
  >
</#if>
<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>

  <div id="metadata" class="metadata ceh-model">
    <div class="section section-grey">
      <div class="container">
        <#if title?? && title?has_content>
            <div id="pageTitle">
              <@b.admin/>
              <h1>
              <#if (metadata.state == 'draft' || metadata.state == 'pending') >
                <small class="text-danger"><b>${codes.lookup('publication.state', metadata.state)?upper_case!''}</b></small>
              </#if>
              ${title}
              <#if (version?? && version?has_content) || (releaseDate?? && releaseDate?has_content)>
                  <small><br>
                    <#if version?? && version?has_content>version ${version}</#if>
                    <#if releaseDate?? && releaseDate?has_content>(${releaseDate})</#if>
                  </small>
              </#if>
              </h1>
            </div>
          </#if>

          <#if description??>
            <div class="description">
              <div class="description-text">
                <@b.linebreaks description />
              </div>
              <div class="browseimage">
                <#if images?? && images?has_content>
                  <img src="${images?first.url}" alt="${images?first.name}"/>
                </#if>
              </div>
            </div>
          </#if>
      </div>
    </div>

    <div class="section">
      <div class="container">

          <#if primaryPurpose?? || SRO??|| otherContacts?? || licenseType?? || onlineResources??>
          <section>
            <#if primaryPurpose?? && primaryPurpose?has_content>
              <@m.key "Primary purpose">
                <@b.linebreaks primaryPurpose />
              </@m.key>
            </#if>

            <#if otherLinks?? && otherLinks?has_content>
              <@m.key "Web links">
                <#list otherLinks as otherLink>
                  <p>
                    <#if otherLink.function?? && otherLink.function?has_content>
                      ${otherLink.function?cap_first}:
                    </#if>
                    <#if otherLink.url?? && otherLink.url?has_content>
                      <@m.Url otherLink.url true/>
                    </#if>
                  </p>
                </#list>
              </@m.key>
            </#if>

            <#if SRO?? && SRO?has_content>
              <@m.key "Senior responsible officer">
                <#list SRO as SRO>
                    <@func.displayContact SRO, true, true, false />
                </#list>
              </@m.key>
            </#if>
            <#if otherContacts?? && otherContacts?has_content>
              <@m.key "Other contacts">
              <#list otherContacts as otherContact>
                <dt>${otherContact.roleDisplayName}</dt>
                <dd>
                  <div class="responsibleParty">
                    <@func.displayContact otherContact, true, true, false />
                  </div>
                </dd>
                </#list>
              </@m.key>
            </#if>

            <#if licenseType?? && licenseType?has_content>
              <@m.key "License" "License type (open or non-open) under which the model is distributed">${licenseType?cap_first}</@m.key>
            </#if>
          </section>
          </#if>

          <#if keyInputVariables?? || keyOutputVariables?? || description?? || modelType?? || currentModelVersion?? || modelCalibration??>
          <section>
            <h2>Model Description</h2>
            <#if keyInputVariables?? && keyInputVariables?has_content>
              <@m.key "Key input variables"><@m.commaList keyInputVariables /></@m.key>
            </#if>
            <#if keyOutputVariables?? && keyOutputVariables?has_content>
              <@m.key "Key output variables" ><@m.commaList keyOutputVariables /></@m.key>
            </#if>
            <#if modelType?? && modelType?has_content>
              <@m.key "Model type" >${modelType?cap_first}</@m.key>
            </#if>
            <#if currentModelVersion?? && currentModelVersion?has_content>
              <@m.key "Current model version">
                ${currentModelVersion}
                <#if releaseDate?? && releaseDate?has_content>
                (${releaseDate})
                </#if>
              </@m.key>
            </#if>
            <#if modelCalibration?? && modelCalibration?has_content>
              <@m.key "Model calibration">${modelCalibration}</@m.key>
            </#if>
          </section>
          </#if>

        <#if spatialDomain?? || spatialResolution?? || temporalResolutionMin?? || temporalResolutionMax?? || boundingBoxes??>
        <section>
          <h2>Spatio-Temporal Information</h2>

          <#if boundingBoxes?has_content>
            <@m.key "Spatial extent">
              <div id="studyarea-map" class="studyarea-map">
                <#list boundingBoxes as extent>
                  <span content="${extent.bounds}" datatype="geoJson"/>
                </#list>
              </div>
            </@m.key>
          </#if>

          <#if spatialDomain?? && spatialDomain?has_content>
            <@m.key "Spatial domain" "Is the model only applicable to certain areas?">${spatialDomain}</@m.key>
          </#if>
          <#if spatialResolution?? && spatialResolution?has_content>
            <@m.key "Spatial resolution" "Spatial resolution at which model works or at which model outputs are generated">${spatialResolution}</@m.key>
          </#if>
          <#if temporalResolutionMin?? && temporalResolutionMin?has_content>
            <@m.key "Temporal resolution (min)" "Minimum time step supported by the model">${temporalResolutionMin}</@m.key>
          </#if>
          <#if temporalResolutionMax?? && temporalResolutionMax?has_content>
            <@m.key "Temporal resolution (max)" "Maximum time step supported by the model ">${temporalResolutionMax}</@m.key>
          </#if>
        </section>
        </#if>

        <#if language?? || compiler?? || operatingSystem?? || systemMemory?? || documentation??>
        <section>
          <h2>Technical Information</h2>
          <#if language?? && language?has_content>
            <@m.key "Language" "Computing language in which the model is written">${language}</@m.key>
          </#if>
          <#if compiler?? && compiler?has_content>
            <@m.key "Compiler" "Compiled required">${compiler}</@m.key>
          </#if>
          <#if operatingSystem?? && operatingSystem?has_content>
            <@m.key "Operating system" "Operating system typically used to run the model">${operatingSystem}</@m.key>
          </#if>
          <#if systemMemory?? && systemMemory?has_content>
            <@m.key "System memory" "Memory required to run code">${systemMemory}</@m.key>
          </#if>
          <#if documentation?? && documentation?has_content>
            <@m.key "Documentation" "Location of technical documentation for the model"><@bareUrl documentation /></@m.key>
          </#if>
        </section>
        </#if>

        <section>
          <h2>QA Information</h2>
          <@m.key "Developer testing" "Use of a range of developer tools including parallel build and analytical review or sense check"><@m.qa developerTesting /></@m.key>
          <@m.key "Internal peer review" "Obtaining a critical evaluation from a third party independent of the development of the model but from within the same organisation"><@m.qa internalPeerReview /></@m.key>
          <@m.key "External peer review" "Formal or informationrmal engagement of a third party to conduct critical evaluation from outside the organisation in which the model is being developed"><@m.qa externalPeerReview /></@m.key>
          <@m.key "Internal model audit" "Formal audit of a model within the organisation, perhaps involving use of internal audit functions"><@m.qa internalModelAudit /></@m.key>
          <@m.key "External model audit" "Formal engagement of external professional to conduct a critical evaluation of the model, perhaps involving audit professionals;"><@m.qa externalModelAudit /></@m.key>
          <@m.key "Quality assurance guidelines and checklists" "Model development refers to department’s guidance or other documented QA processes (e.g. third party publications)"><@m.qa qaGuidelinesAndChecklists /></@m.key>
          <@m.key "Governance" "At least one of planning, design and/or sign-off of model for use is referred to a more senior person.  There is a clear line of accountability for the model"><@m.qa governance /></@m.key>
          <@m.key "Transparency" "Model is placed in the wider domain for scrutiny, and/or results are published"><@m.qa transparency /></@m.key>
          <@m.key "Periodic review" "Model is reviewed at intervals to ensure it remains fit for the intended purpose, if used on an ongoing basis"><@m.qa periodicReview /></@m.key>
        </section>

        <#if versionHistories?? && versionHistories?has_content>
          <section>
           <h2>Version control change notes</h2>
            <#list versionHistories as history>
            <@b.repeatRow>
              <#if history.version?? && history.version?has_content>
                <@b.basicRow>
                  <@m.keyContent "Version">${history.version}</@m.keyContent>
                </@b.basicRow>
              </#if>
              <#if history.date?? && history.date?has_content>
                <@b.basicRow>
                  <@m.keyContent "Date">${history.date}</@m.keyContent>
                </@b.basicRow>
              </#if>
              <#if history.note?? && history.note?has_content>
                <@b.basicRow>
                  <@m.keyContent "Note">${history.note}</@m.keyContent>
                </@b.basicRow>
              </#if>
            </@b.repeatRow>
            </#list>
            </section>
          </#if>

          <#assign modelApplications=jena.modelApplications(uri)/>
            <#if projectUsages?? && projectUsages?has_content || modelApplications?has_content>
            <section>
            <h2>Project use</h2>
            <#if projectUsages?? && projectUsages?has_content>
              <@m.key "Projects">
                <#list projectUsages as usage>
                  <div class="projectList">
                    <#if usage.project??><span class="projectList__project">${usage.project}</span></#if>
                    <#if usage.version??><span class="projectList_version">version ${usage.version}</span></#if>
                    <#if usage.date??><span class="projectList__date">(${usage.date?date?string['MMMM yyyy']})</span></#if>
                  </div>
                </#list>
              </@m.key>
            </#if>
            <#--noinspection FtlWellformednessInspection-->
            <#list modelApplications>
              <@m.key "Model use" "Implementations of the model">
                <#items as md>
                  <@b.blockUrl md/>
                </#items>
              </@m.key>
            </#list>
            </section>
          </#if>


        <#if references?? && references?has_content>
        <section>
          <h2>References</h2>

          <#list references as ref>
          <@b.repeatRow>
            <#if ref.citation?? && ref.citation?has_content>
              <span class="reference-citation">${ref.citation}</span>
            </#if>
            <#if ref.doi?? && ref.doi?has_content>
              <span class="reference-doi"><@m.Url ref.doi true/></span>
            </#if>
            <#if ref.nora?? && ref.nora?has_content>
              <span class="reference-nora">${ref.nora}</span>
            </#if>
          </@b.repeatRow>
          </#list>
        </section>
        </#if>

        <@m.additionalMetadata />
      </div>
    </div>

</@skeleton.master>

<#--
A url that repeats the url as the link text
-->
<#macro bareUrl value>
    <a href="${value}">${value}</a>
</#macro>

