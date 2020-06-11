<#import "skeleton.ftl" as skeleton>
<#import "blocks.ftl" as b>
<#import "ceh-model-macros.ftl" as m>
<#import "../functions.tpl" as func>

<#if responsibleParties?? && responsibleParties?has_content>
  <#assign
    SRO = func.filter(responsibleParties, "role", "owner")
    otherContacts = func.filter(responsibleParties, "role", "owner", true)
  >
</#if>
<#if onlineResources?? && onlineResources?has_content>
  <#assign
    image = func.filter(onlineResources, "function", "browseGraphic")
    repo = func.filter(onlineResources, "function", "code")
    documentation = func.filter(onlineResources, "function", "documentation")
    otherLinks = func.filter(func.filter(func.filter(onlineResources, "function", "documentation", true), "function", "code", true), "function", "browseGraphic", true)
  >
</#if>
<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)><#escape x as x?html>

  <@b.metadataContainer "ceh-model">

    <@b.admin/>

    <#if title?? && title?has_content>
      <h1 class="document-title"><small>Model</small> ${title}</h1>
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


    <div class="row">
      <div class="col-sm-4 col-sm-push-8">
        <#if onlineResources?size gte 1 || licenseType?? >
          <div class="panel-right distribution">
            <#if repo?? && repo?has_content>
              <p class="panel-title">Get the code</p> 
                <ul class="list-unstyled">
                <#list repo as link>
                  <li>
                    <#if link.url?? && link.url?has_content>
                      <@m.Url link.url true/>
                    </#if>
                  </li>
                </#list>
                </ul>
              </#if>
              <!--<#if documentation?? && documentation?has_content>
                <p class="panel-title">Documentation</p> 
                <ul class="list-unstyled">
                <#list documentation as link>
                  <li>
                    <#if link.url?? && link.url?has_content>
                      <@m.Url link.url true/>
                    </#if>
                  </li>
                </#list>
                </ul>
              </#if>-->
              <#if licenseType?? && licenseType?has_content>
              <p class="panel-title">Licence</p> 
              <p>${licenseType?cap_first}</p>
              </#if>
          </div>
          </#if>
      </div>
      <div class="col-sm-8 col-sm-pull-4">
        <#if primaryPurpose?? && primaryPurpose?has_content>
          <@m.key "Primary purpose">
              <@b.linebreaks primaryPurpose />
          </@m.key>
        </#if>
        <@m.key "Senior responsible officer">
          <#if SRO?? && SRO?has_content>
            <#noescape>
              <#list SRO as SRO>
                ${func.displayContact(SRO, true, true, false)}
              </#list>
            </#noescape>
          <#else>
            <b class="text-danger"><i class="fas fa-exclamation"> </i> There is no SRO</b>
          </#if>
        </@m.key>

      </div>
     </div>

    <#if  otherContacts?? || otherLinks??>
      <section>  
      <#if otherContacts?? && otherContacts?has_content>
        <@m.key "Other contacts">
        <#noescape>
        <#list otherContacts as otherContact>
          <dt>${otherContact.roleDisplayName?html}</dt>
          <dd>
            <div class="responsibleParty">      
              ${func.displayContact(otherContact, true, true, false)}
            </div>
          </dd>
          </#list>
        </#noescape>
        </@m.key>
      </#if>

      <#if otherLinks?? && otherLinks?has_content>
        <@m.key "Other links">
          <#list otherLinks as link>
            <p>
              <#if link.function?? && link.function?has_content>
                <span>${link.function?cap_first}</span>
              </#if>
              <#if link.url?? && link.url?has_content>
                <@m.Url link.url true/>
              </#if>
            </p>
          </#list>
        </@m.key>
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
          <div id="studyarea-map">
            <#list boundingBoxes as extent>
              <span content="${extent.wkt?html}" datatype="geo:wktLiteral"/>
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

    <#if language?? || compiler?? || operatingSystem?? || systemMemory?? >
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
      <#if versionHistories?? && versionHistories?has_content>
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
      </#if>
      
      <#assign modelApplications=jena.modelApplications(uri)/>
      <#if projectUsages?? && projectUsages?has_content || modelApplications?has_content>

        <h2>Project use</h2>
        <#if projectUsages?? && projectUsages?has_content>
          <@m.key "Projects">
            <#list projectUsages as usage>

                <#if usage.project??>${usage.project}</#if>
                <#if usage.version??>version ${usage.version}</#if>
                <#if usage.date??>(${usage.date?date?string['MMMM yyyy']})</#if>
                <#sep><br></#sep>

              <div class="projectList">
                <#if usage.project??><span class="projectList__project">${usage.project}</span></#if>
                <#if usage.version??><span class="projectList_version">version ${usage.version}</span></#if>
                <#if usage.date??><span class="projectList__date">(${usage.date?date?string['MMMM yyyy']})</span></#if>
              </div>

            </#list>
          </@m.key>
        </#if>
        <#list modelApplications>
          <@m.key "Model implementations">
            <#items as md>
              <@b.blockUrl md/>
            </#items>
          </@m.key>   
        </#list>
      </#if>
    </section>



    <#if references?? && references?has_content>

    <section>
      <h2>References</h2>
        <#list supplemental as item>
        <@b.repeatRow>
          <#if item.description?? && item.description?has_content>
            <span class="supplemental-description">${item.description}</span>
          </#if>
          <#if item.url?? && item.url?has_content>
            <span class="supplemental-url"><@m.Url item.url true/></span>
          </#if>
          <#if item.noraID?? && item.noraID?has_content && item.noraID?matches("\\d{3,10}")>
            <span class="supplemental-nora"><br><a href="http://nora.nerc.ac.uk/id/eprint/${item.noraID}">View in NORA &raquo;</a></span>
          </#if>
        </@b.repeatRow>
        </#list>
    </section>
    </#if>

    <@m.additionalMetadata />

      
  </@b.metadataContainer>
</#escape></@skeleton.master>