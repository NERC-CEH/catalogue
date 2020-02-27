<#import "skeleton.ftl" as skeleton>
<#import "blocks.ftl" as b>
<#import "ceh-model-macros.ftl" as m>
<#import "../functions.tpl" as func>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)><#escape x as x?html>
  <@b.metadataContainer "ceh-model">
    
    <@b.admin />

    <#if title?? || projectCode?? || projectObjectives?? || description?? || keywords?? || projectCompletionDate?? || projectWebsite?? || funderDetails?? || multipleModelsUsed?? || multipleModelLinkages??>
    <section>
      <h1 class="document-title"><small>Model implementation</small>${title}</h1>
      <h2>Project Information</h2>

      <#if projectCode?? && projectCode?has_content>
        <@m.key "Project code" "RMS project code">${projectCode}</@m.key>
      </#if>

      <#if projectObjectives?? && projectObjectives?has_content>
        <@m.key "Objectives" "Brief description of main objectives">
          <#noescape>
            <@b.linebreaks projectObjectives />
          </#noescape>
        </@m.key>
      </#if>
      <#if description?? && description?has_content>
        <@m.key "Description" "Longer description of project including why models were used to answer the science question, assumptions made, key outputs">
          <#noescape>
            <@b.linebreaks description />
          </#noescape>
        </@m.key>
      </#if>
      <#if projectCompletionDate?? && projectCompletionDate?has_content>
        <@m.key "Project completion date" "Project end date">${projectCompletionDate?date?string['d MMMM yyyy']}</@m.key>
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
     
      <#if responsibleParties??>
        <@m.key "Contact" "">
          <#list responsibleParties as item>
              ${item.individualName}
              <#if item.email?has_content && item.email?matches("\\b[\\w\\.-]+@[\\w\\.-]+\\.\\w{2,4}\\b")>
                (<a href="mailto:${item.email}">${item.email}</a>)
              <#sep><br></#sep>
              </#if>
          </#list>
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

    <#if supplemental?? && supplemental?has_content>
    <section>
      <h2>References</h2>

      <#list supplemental as item>
      <@b.repeatRow>
        <#if item.description?? && item.description?has_content>
          <span class="reference-description">${item.description}</span>
        </#if>
        <#if item.url?? && item.url?has_content>
          <span class="reference-url"><@m.Url item.url true/></span>
        </#if>
        <#if item.noraID?? && item.noraID?has_content>
          <span class="reference-nora">${item.noraID}</span>
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
              <@m.keyContent "Model">
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
              <@m.keyContent "Spatial extent">${modelInfo.spatialExtentOfApplication?cap_first}</@m.keyContent>
            </@b.basicRow>
          </#if>
          <#if modelInfo.availableSpatialData?? && modelInfo.availableSpatialData?has_content>
            <@b.basicRow>
              <@m.keyContent "Available spatial data">${modelInfo.availableSpatialData?cap_first}</@m.keyContent>
            </@b.basicRow>
          </#if>
          <#if modelInfo.spatialResolutionOfApplication?? && modelInfo.spatialResolutionOfApplication?has_content>
            <@b.basicRow>
              <@m.keyContent "Spatial resolution" >${modelInfo.spatialResolutionOfApplication}</@m.keyContent>
            </@b.basicRow>
          </#if>
          <#if modelInfo.temporalExtentOfApplicationStartDate??||  modelInfo.temporalExtentOfApplicationEndDate?? >
            <@b.basicRow><@m.keyContent "Temporal extent">
                <#if modelInfo.temporalExtentOfApplicationStartDate?? && modelInfo.temporalExtentOfApplicationStartDate?has_content>
                  From <span>${modelInfo.temporalExtentOfApplicationStartDate?date?string['d MMMM yyyy']}</span>
                <#else>
                &hellip;
                </#if>
                <#if modelInfo.temporalExtentOfApplicationEndDate?? && modelInfo.temporalExtentOfApplicationEndDate?has_content>
                  to <span>${modelInfo.temporalExtentOfApplicationEndDate?date?string['d MMMM yyyy']}</span>
                </#if>
            </@m.keyContent></@b.basicRow>
          </#if>
          <#if modelInfo.temporalResolutionOfApplication?? && modelInfo.temporalResolutionOfApplication?has_content>
            <@b.basicRow>
              <@m.keyContent "Temporal resolution">${modelInfo.temporalResolutionOfApplication}</@m.keyContent>
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

    <#if inputData?? && inputData?size gt 0 >
    <section>
      <h2>Input data</h2>
      <@dataInfoTable inputData />
    </section>
    </#if>

    <#if outputData?? && outputData?size gt 0>
    <section>
      <h2>Output data</h2>
      <@dataInfoTable outputData />
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


<#macro dataInfoTable data> 
<table class="table table-condensed">
<thead><tr><th>variable name</th><th>units</th><th>file format</th><th>url</th></tr></thead>
<tbody>
  <#list data as item>
    <tr>
      <td>
        <#if item.variableName?? && item.variableName?has_content>${item.variableName}</#if>
      </td>
      <td>
        <#if item.units?? && item.units?has_content>${item.units}</#if>
      </td>
      <td>
        <#if item.fileFormat?? && item.fileFormat?has_content>${item.fileFormat}</#if>
      </td>
      <td>
        <#if item.url?? && item.url?has_content><@b.bareUrl item.url/></#if>
      </td>
    </tr>
  </#list>
</tbody>
</table>
</#macro>