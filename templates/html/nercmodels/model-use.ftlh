<#import "../skeleton.ftlh" as skeleton>
<#import "../../functions.ftlh" as func>
<#import "../blocks.ftlh" as b>
<#import "macros.ftlh" as m>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
  <@b.metadataContainer "ceh-model">

    <@b.admin />

    <#if title?? && title?has_content>
      <h1>${title}</h1>
    </#if>

    <#if objectives?? && objectives?has_content>
      <@m.key "Objectives">
        <@b.linebreaks objectives />
      </@m.key>
    </#if>

    <#if description?? && description?has_content>
      <@m.key "Description">
        <@b.linebreaks description />
      </@m.key>
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

    <#if rooferences?? && rooferences?has_content>
    <section>
      <h2>Rooferences</h2>

      <#list rooferences as ref>
      <@b.repeatRow>
        <#if ref.name?? && ref.name?has_content>
         ${ref.name}
        </#if>
        <#if ref.url?? && ref.url?has_content>
          <@m.Url ref.url true/>
        </#if>
        <#if ref.description?? && ref.description?has_content>
          ${ref.description}
        </#if>
      </@b.repeatRow>
      </#list>
    </section>
    </#if>


    <#if modelInfos?? && modelInfos?has_content>

      <section>
      <h2>Model Information</h2>
        <#list modelInfos as modelInfo>
        <@b.repeatRow>
         <#if (modelInfo.id?? && modelInfo.id?has_content) || (modelInfo.name?? && modelInfo.name?has_content)>
            <#assign model=jena.metadata(modelInfo.id)!""/>
            <@b.basicRow>
              <@m.keyContent "Model name">
                <#if model?has_content>
                  <a href="${model.href}">${model.title}</a>
                <#else>
                  ${modelInfo.name}
                  <#if modelInfo.id?? && modelInfo.id?has_content>
                    (${modelInfo.id})
                  </#if>
                </#if>
              </@m.keyContent>
            </@b.basicRow>
          </#if>
          <#if modelInfo.version?? && modelInfo.version?has_content>
            <@b.basicRow>
              <@m.keyContent "Version">${modelInfo.version}</@m.keyContent>
            </@b.basicRow>
          </#if>
          <#if modelInfo.rationale?? && modelInfo.rationale?has_content>
            <@b.basicRow>
              <@m.keyContent "Rationale">${modelInfo.rationale}</@m.keyContent>
            </@b.basicRow>
          </#if>
          <#if modelInfo.spatialExtentOfApplication?? && modelInfo.spatialExtentOfApplication?has_content>
            <@b.basicRow>
              <@m.keyContent "Spatial extent of application">${modelInfo.spatialExtentOfApplication?cap_first}</@m.keyContent>
            </@b.basicRow>
          </#if>
          <#if modelInfo.availableSpatialData?? && modelInfo.availableSpatialData?has_content>
            <@b.basicRow>
              <@m.keyContent "Available spatial data">${modelInfo.availableSpatialData?cap_first}</@m.keyContent>
            </@b.basicRow>
          </#if>
          <#if modelInfo.spatialResolutionOfApplication?? && modelInfo.spatialResolutionOfApplication?has_content>
            <@b.basicRow>
              <@m.keyContent "Spatial resolution of application" >${modelInfo.spatialResolutionOfApplication}</@m.keyContent>
            </@b.basicRow>
          </#if>
          <#if modelInfo.temporalExtentOfApplicationStartDate?? && modelInfo.temporalExtentOfApplicationStartDate?has_content>
            <@b.basicRow>
              <@m.keyContent "Temporal extent of application (start date)">${modelInfo.temporalExtentOfApplicationStartDate}</@m.keyContent>
            </@b.basicRow>
          </#if>
          <#if modelInfo.temporalExtentOfApplicationEndDate?? && modelInfo.temporalExtentOfApplicationEndDate?has_content>
            <@b.basicRow>
              <@m.keyContent "Temporal extent of application (end date)">${modelInfo.temporalExtentOfApplicationEndDate}</@m.keyContent>
            </@b.basicRow>
          </#if>
          <#if modelInfo.temporalResolutionOfApplication?? && modelInfo.temporalResolutionOfApplication?has_content>
            <@b.basicRow>
              <@m.keyContent "Temporal resolution of application">${modelInfo.temporalResolutionOfApplication}</@m.keyContent>
            </@b.basicRow>
          </#if>
          <#if modelInfo.calibrationConditions?? && modelInfo.calibrationConditions?has_content>
            <@b.basicRow>
              <@m.keyContent "Calibration conditions">${modelInfo.calibrationConditions}</@m.keyContent>
            </@b.basicRow>
          </#if>
        </@b.repeatRow>
        </#list>
      </section>
    </#if>

    <#if inputData?? || outputData??>
    <section>
      <h2>Data Information</h2>

        <#if inputData??>
          <@m.key "Input data">
            <@m.dataInfoTable inputData />
          </@m.key>
         </#if>

        <#if outputData??>
          <@m.key "Output data">
            <@m.dataInfoTable outputData />
          </@m.key>
         </#if>
    </section>
    </#if>

    <#if sensitivityAnalysis?? || uncertaintyAnalysis?? || validation??>
    <section>
      <h2>Evaluation Information</h2>
      <#if sensitivityAnalysis?? && sensitivityAnalysis?has_content>
        <@m.key "Sensitivity analysis" "Details of any sensitivity analysis performed, or link to appropriate documentation">
          <@b.linebreaks sensitivityAnalysis />
        </@m.key>
      </#if>
      <#if uncertaintyAnalysis?? && uncertaintyAnalysis?has_content>
        <@m.key "Uncertainty analysis" "How was uncertainty in the model captured and represented? Give links to any appropriate documentation">
          <@b.linebreaks uncertaintyAnalysis />
        </@m.key>
      </#if>
      <#if validation?? && validation?has_content>
        <@m.key "Uncertainty analysis" "Was the model validated against data not used for model building or parameterisation? If so, provide links to any documentation of results">
          <@b.linebreaks validation />
        </@m.key>
      </#if>
    </section>
    </#if>

    <@m.additionalMetadata />

  </@b.metadataContainer>
  <#include "_footer.ftlh">

</@skeleton.master>
