<#import "skeleton.ftlh" as skeleton>
<#import "blocks.ftlh" as b>
<#import "nerc-model-macros.ftlh" as m>
<#import "../functions.ftlh" as func>

<#if onlineResources?? && onlineResources?has_content>
  <#assign
    image = func.filter(onlineResources, "function", "browseGraphic")
    otherLinks = func.filter(onlineResources, "function", "browseGraphic", true)
  >
</#if>
<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>

  <@b.metadataContainer "ceh-model">

    <@b.admin/>

    <#if title?? && title?has_content>
      <h1>${title}</h1>
    </#if>

    <#if description?? && description?has_content>
        <div class="description clearfix">
          <#if image?? && image?has_content>
            <#if image?first.url?matches("^http(|s)://.+(jpg|jpeg|gif|png)$")>
              <img src="${image?first.url}" alt="${image?first.name}" class="browseGraphic" />
            </#if>
          </#if>
          <@b.linebreaks description />
        </div>
    </#if>

      <#if purpose?? || responsibleParties?? || licenseType?? || onlineResources??>
      <div>
        <#if purpose?? && purpose?has_content>
          <@m.key "Purpose">
            <@b.linebreaks purpose />
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

        <#if responsibleParties?? && responsibleParties?has_content>
          <@m.key "Contacts">
          <#list responsibleParties as responsibleParty>
            <dt>${responsibleParty.roleDisplayName}</dt>
            <dd>
              <div class="responsibleParty">
                <@func.displayContact responsibleParty, true, true, false />
              </div>
            </dd>
            </#list>
          </@m.key>
        </#if>

        <#if licenseType?? && licenseType?has_content>
          <@m.key "License" "License type (open or non-open) under which the model is distributed">${licenseType?cap_first}</@m.key>
        </#if>
      </div>
      </#if>

      <#if keyInputVariables?? || keyOutputVariables?? || modelType?? || currentModelVersion?? || modelCalibration??>
      <div>
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
      </div>
      </#if>

    <#if spatialDomain?? || spatialResolution?? || temporalResolutionMin?? || temporalResolutionMax?? || boundingBoxes??>
    <div>
      <h2>Spatio-Temporal Information</h2>

      <#if boundingBoxes?has_content>
        <@m.key "Spatial extent">
          <div id="studyarea-map">
            <#list boundingBoxes as extent>
              <span content="${extent.wkt}" datatype="geo:wktLiteral"/>
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
    </div>
    </#if>

    <#if language?? || compiler?? || operatingSystem?? || systemMemory?? || documentation??>
    <div>
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
    </div>
    </#if>


    <div>
      <h2>Quality assurance</h2>
      <#if qa?? && qa?has_content>
        <div class="modelgrid modelgrid--3 modelgrid--qa">
          <div><strong>Date</strong></div>
          <div><strong>Type</strong></div>
          <div><strong>Notes</strong></div>
        <#list qa as q>
          <div><#if q.date?has_content>${q.date}</#if></div>
          <div><#if q.type?has_content>${q.type}</#if></div>
          <div><#if q.notes?has_content>${q.notes}</#if></div>
        </#list>
        </div>
      <#else>
        <p class="text-danger">No quality assurance information provided</p>
      </#if>
    </div>

    <div>
    <#assign nercModelUses=jena.nercModelUses(uri)/>
      <#if nercModelUses?has_content>
        <h2>Implementations of the model</h2>
        <#--noinspection FtlWellformednessInspection-->
        <#list nercModelUses>
          <@m.key "Model use" "Implementations of the model">
            <#items as md>
              <@b.blockUrl md/>
            </#items>
          </@m.key>
        </#list>
      </#if>
    </div>

    <#if references?? && references?has_content>
    <div>
      <h2>References</h2>
        <div class="modelgrid modelgrid--3 modelgrid--references">
          <div><strong>Name</strong></div>
          <div><strong>Description</strong></div>
          <div><strong>URL</strong></div>
      <#list references as ref>
        <div><#if ref.name?? && ref.name?has_content>${ref.name}</#if></div>
        <div><#if ref.description?? && ref.description?has_content>${ref.description}</#if></div>
        <div><#if ref.url?? && ref.url?has_content><@m.Url ref.url true/></#if></div>
      </#list>
      </div>
    </div>
    </#if>

    <@m.additionalMetadata />


  </@b.metadataContainer>
</@skeleton.master>
