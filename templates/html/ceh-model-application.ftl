<#import "skeleton.ftl" as skeleton>
<#import "blocks.ftl" as b>
<#import "ceh-model-macros.ftl" as m>
<#import "../functions.tpl" as func>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)><#escape x as x?html>
  <@b.metadataContainer "ceh-model">
    
    <@b.admin />

    <#if title?? || projectCode?? || projectObjectives?? || description?? || keywords?? || projectCompletionDate?? || boundingBoxes?? ||projectWebsite?? || funderDetails?? || multipleModelsUsed?? || multipleModelLinkages??>
    <section>
      <h1 class="document-title"><small>Model implementation</small>${title}</h1>
      <h2>Project Information</h2>

      <#if description?? && description?has_content>
        <@m.key "Description">
          <#noescape>
            <@b.linebreaks description />
          </#noescape>
        </@m.key>
      </#if>
      <#if projectObjectives?? && projectObjectives?has_content>
        <@m.key "Objectives">
          <#noescape>
            <@b.linebreaks projectObjectives />
          </#noescape>
        </@m.key>
      </#if>
      <#if boundingBoxes?has_content>
        <@m.key "Spatial extent">
          <div id="studyarea-map">
            <#list boundingBoxes as extent>
              <span content="${extent.wkt?html}" datatype="geo:wktLiteral"/>
            </#list>
          </div>
        </@m.key>
      </#if>

      <#if spatialResolution?? && spatialResolution?has_content>
        <@m.key "Spatial resolution">${spatialResolution}</@m.key>
      </#if>
      <#if temporalExtents?? && temporalExtents?has_content>
        <@m.key "Temporal extent">
         <#list temporalExtents as extent>
            <p>
            <#if extent.begin?has_content>
              <#if !extent.end?has_content>Starts </#if>
              <span class="extentBegin">${extent.begin?date?string['d MMMM yyyy']}</span>
              <#if extent.end?has_content> to </#if>
            </#if>
            <#if extent.end?has_content>
              <#if !extent.begin?has_content>Ends </#if>
              <span class="extentEnd">${extent.end?date?string['d MMMM yyyy']}</span>
            </#if>
            </p>         
          </#list>
        </@m.key>
      </#if>
      <#if temporalResolution?? && temporalResolution?has_content>
        <@m.key "Temporal resolution">${temporalResolution}</@m.key>
      </#if>


      <#if projectCompletionDate?? && projectCompletionDate?has_content>
        <@m.key "Completion date">${projectCompletionDate?date?string['d MMMM yyyy']}</@m.key>
      </#if>
      <#if projectWebsite?? && projectWebsite?has_content>
        <@m.key "Project website"><@b.bareUrl projectWebsite /></@m.key>
      </#if>
      <#if projectCode?? && projectCode?has_content>
        <@m.key "Project code">${projectCode}</@m.key>
      </#if>
      <#if funderDetails?? && funderDetails?has_content>
        <@m.key "Funder details">
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
        <@m.key "Multiple models used?">
          <#noescape>
            <@b.linebreaks multipleModelsUsed />
          </#noescape>
        </@m.key>
      </#if>
      <#if multipleModelLinkages?? && multipleModelLinkages?has_content>
        <@m.key "Multiple model linkages">
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
      <h2>Model<#if modelInfos?size gt 1>s</#if></h2>
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
          <#if modelInfo.calibrationConditions?? && modelInfo.calibrationConditions?has_content>
            <@b.basicRow>
              <@m.keyContent "Calibration conditions">${modelInfo.calibrationConditions}</@m.keyContent>
            </@b.basicRow>
          </#if>
        </@b.repeatRow>
        </#list>
      </section>
    </#if>


    <#if inputVariables?? && inputVariables?size gt 0 >
    <section>
      <h2>Input variables</h2>
      <@variablesgrid inputVariables />
    </section>
    </#if>

    <#if outputVariables?? && outputVariables?size gt 0 >
    <section>
      <h2>Output variables</h2>
      <@variablesTable outputVariables />
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

<#macro variablesTable data> 
<table class="table table-condensed">
<thead><tr><th>Name</th><th>Title</th><th>Description</th><th>Units</th><th>Format</th></tr></thead>
<tbody>
  <#list data as item>
    <tr>
      <td>
        <#if item.name?? && item.name?has_content>${item.name}</#if>
      </td>
      <td>
        <#if item.title?? && item.title?has_content>${item.title}</#if>
      </td>
      <td>
        <#if item.description?? && item.description?has_content>${item.description}</#if>
      </td>
      <td>
        <#if item.type?? && item.type?has_content>${item.type}</#if>
      </td>
      <td>
        <#if item.format?? && item.format?has_content>${item.format}</#if>
      </td>
      
    </tr>
  </#list>
</tbody>
</table>
</#macro>


<#macro variablesgrid data> 
<div class="variable-grid">
  <#list data as item>
    <div class="variable">
        <#if item.name?? && item.name?has_content><div class="variable--label">Name</div><div class="variable--value">${item.name}</div></#if>
        <#if item.title?? && item.title?has_content><div class="variable--label">Title</div><div class="variable--value">${item.title}</div></#if>
        <#if item.description?? && item.description?has_content><div class="variable--label">Description</div><div class="variable--value">${item.description}</div></#if>
        <#if item.type?? && item.type?has_content><div class="variable--label">Type</div><div class="variable--value">${item.type}</div></#if>
        <#if item.format?? && item.format?has_content><div class="variable--label">Format</div><div class="variable--value">${item.format}</div></#if>
        <#if item.constraints?? && item.constraints?has_content>
        <div class="variable--label">Constraints</div>
        <div class="variable--constraints">
          <#if item.constraints.minimum?? && item.constraints.minimum?has_content><div class="variable--label">Minimum</div><div class="variable--value">${item.constraints.minimum}</div></#if>
          <#if item.constraints.maximum?? && item.constraints.maximum?has_content><div class="variable--label">Maximum</div><div class="variable--value">${item.constraints.maximum}</div></#if>
          <#if item.constraints.minLength?? && item.constraints.minLength?has_content><div class="variable--label">Min length</div><div class="variable--value">${item.constraints.minLength}</div></#if>
          <#if item.constraints.maxLength?? && item.constraints.maxLength?has_content><div class="variable--label">Max length</div><div class="variable--value">${item.constraints.maxLength}</div></#if>
          <#if item.constraints.unique?? && item.constraints.unique==true><div class="variable--label">Unique</div><div class="variable--value">Yes</div></#if>
        </div>
        </#if>


    </div>
  </#list>

</div>
</#macro>