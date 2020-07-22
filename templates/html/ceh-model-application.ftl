<#import "skeleton.ftl" as skeleton>
<#import "blocks.ftl" as b>
<#import "ceh-model-macros.ftl" as m>
<#import "../functions.tpl" as func>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)><#escape x as x?html>
  <@b.metadataContainer "ceh-model">
    
    <@b.admin />

    <#if title?? || projectCode?? || projectObjectives?? || description?? || keywords?? || projectCompletionDate?? || projectWebsite?? || funderDetails?? || contactName?? || multipleModelsUsed?? || multipleModelLinkages??>
    <section>
      <h2>Project Information</h2>

      <#if title?? && title?has_content>
        <@m.key "Project title" "Title of project">${title}</@m.key>
      </#if>

      <#if projectCode?? && projectCode?has_content>
        <@m.key "Project code" "RMS project code">${projectCode}</@m.key>
      </#if>

      <#if projectObjectives?? && projectObjectives?has_content>
        <@m.key "Project objectives" "Brief description of main objectives">
          <#noescape>
            <@b.linebreaks projectObjectives />
          </#noescape>
        </@m.key>
      </#if>
      <#if description?? && description?has_content>
        <@m.key "Project description" "Longer description of project including why models were used to answer the science question, assumptions made, key outputs">
          <#noescape>
            <@b.linebreaks description />
          </#noescape>
        </@m.key>
      </#if>
      <#if projectCompletionDate?? && projectCompletionDate?has_content>
        <@m.key "Project completion date" "Project end date">${projectCompletionDate}</@m.key>
      </#if>
      <#if projectWebsite?? && projectWebsite?has_content>
        <@m.key "Project website" "Link to public-facing website if available"><@b.bareUrl projectWebsite /></@m.key>
      </#if>
      <#if funderDetails?? && funderDetails?has_content>
        <@m.key "Funder details" "Funder details including grant number if appropriate">
          <#noescape>
            <@b.linebreaks funderDetails />
          </#noescape>
        </@m.key>
      </#if>
      <#if contactName??>
        <@m.key "Contact name" "Name of CEH PI/project representative">
          <#if contactName?has_content>
            ${contactName}
          </#if>
          <#if contactEmail?? && contactEmail?has_content>
            (${contactEmail})
          </#if>
        </@m.key>
      </#if>
      <#if multipleModelsUsed?? && multipleModelsUsed?has_content>
        <@m.key "Multiple models used?" "Were multiple models used in the project? If so, which ones?">
          <#noescape>
            <@b.linebreaks multipleModelsUsed />
          </#noescape>
        </@m.key>
      </#if>
      <#if multipleModelLinkages?? && multipleModelLinkages?has_content>
        <@m.key "Multiple model linkages" "If multiple models were used how was this done e.g. chained, independent runs, comparisons, ensemble">
          <#noescape>
            <@b.linebreaks multipleModelLinkages />
          </#noescape>
        </@m.key>
      </#if>
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
          <#noescape>
            <@b.linebreaks sensitivityAnalysis />
          </#noescape>
        </@m.key>
      </#if>
      <#if uncertaintyAnalysis?? && uncertaintyAnalysis?has_content>
        <@m.key "Uncertainty analysis" "How was uncertainty in the model captured and represented? Give links to any appropriate documentation">
          <#noescape>
            <@b.linebreaks uncertaintyAnalysis />
          </#noescape>
        </@m.key>
      </#if>
      <#if validation?? && validation?has_content>
        <@m.key "Uncertainty analysis" "Was the model validated against data not used for model building or parameterisation? If so, provide links to any documentation of results">
          <#noescape>
            <@b.linebreaks validation />
          </#noescape>
        </@m.key>
      </#if>
    </section>
    </#if>

    <@m.additionalMetadata />
    
  </@b.metadataContainer>
</#escape></@skeleton.master>