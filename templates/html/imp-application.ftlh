<#import "blocks.ftlh" as blocks>
<#import "skeleton.ftlh" as skeleton>
<#import "../underscore.ftlh" as _>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
  <div id="metadata" class="metadata container">

    <@blocks.title title type />
    <@blocks.description description!"" />
    <div class="row">
      <div class="col-sm-4 col-sm-push-8">
        <#include "imp/_admin.ftlh">
        <@blocks.links jena.datasets(uri) "Datasets" />
        <@blocks.links jena.models(uri) "Models" />
      </div>
      <div class="col-sm-8 col-sm-pull-4">
        <div class="row key-value">
          <#if relevanceToCaMMP?has_content>
          <div class="key col-sm-3">Relevance to CaMMPdd</div><div class="value col-sm-9">${relevanceToCaMMP}</div>
          </#if>
          <#if date?has_content>
          <div class="key col-sm-3">Datedd</div><div class="value col-sm-9">${date}</div>
          </#if>
          <#if studySite?has_content>
          <div class="key col-sm-3">Study Sitedd</div><div class="value col-sm-9">${studySite}</div>
          </#if>
          <#if objective?has_content>
          <div class="key col-sm-3">Objectivedd</div><div class="value col-sm-9">${objective}</div>
          </#if>
          <#if funderDetails?has_content>
          <div class="key col-sm-3">Funder Detailsdd</div><div class="value col-sm-9">${funderDetails}</div>
          </#if>
          <#if contact??>
            <div class="key col-sm-3">Contactdd</div>
            <div class="value col-sm-9">
              <#if contact.individualName?has_content>${contact.individualName}</#if><#if contact.organisationName?has_content>, ${contact.organisationName}</#if>
            </div>
          </#if>
          <#if keywords??>
          <div class="key col-sm-3">Keywordsdd</div><div class="value col-sm-9"><@blocks.keywords keywords/></div>
          </#if>
          <#if multipleModelsUsed?has_content>
          <div class="key col-sm-3">Multiple Models Useddd</div><div class="value col-sm-9">${multipleModelsUsed}</div>
          </#if>
          <#if multipleModelLinkages?has_content>
          <div class="key col-sm-3">Multiple Model Linkagesdd</div><div class="value col-sm-9">${multipleModelLinkages}</div>
          </#if>
          <#if sensitivity?has_content>
          <div class="key col-sm-3">Sensitivitydd</div><div class="value col-sm-9">${sensitivity}</div>
          </#if>
          <#if uncertainty?has_content>
          <div class="key col-sm-3">Uncertaintydd</div><div class="value col-sm-9">${uncertainty}</div>
          </#if>
          <#if validation?has_content>
          <div class="key col-sm-3">Validationdd</div><div class="value col-sm-9">${validation}</div>
          </#if>
          <#if modelEasyToUse?has_content>
          <div class="key col-sm-3">Model Easy To Usedd</div><div class="value col-sm-9">${modelEasyToUse}</div>
          </#if>
          <#if userManualUseful?has_content>
          <div class="key col-sm-3">User Manual Usefuldd</div><div class="value col-sm-9">${userManualUseful}</div>
          </#if>
          <#if dataObtainable?has_content>
          <div class="key col-sm-3">Data Obtainabledd</div><div class="value col-sm-9">${dataObtainable}</div>
          </#if>
          <#if modelUnderstandable?has_content>
          <div class="key col-sm-3">Model Understandabledd</div><div class="value col-sm-9">${modelUnderstandable}</div>
          </#if>
        </div>
        <#if models?? >
          <h2>Models</h2>
          <#list models as model>
          <div class="row key-value">
            <#if model.name?has_content>
            <div class="key col-sm-3">Namedd</div><div class="value col-sm-9">${model.name}</div>
            </#if>
            <#if model.version?has_content>
            <div class="key col-sm-3">Versiondd</div><div class="value col-sm-9">${model.version}</div>
            </#if>
            <#if model.primaryPurpose?has_content>
            <div class="key col-sm-3">Primary Purposedd</div><div class="value col-sm-9">${model.primaryPurpose}</div>
            </#if>
            <#if model.applicationScale?has_content>
            <div class="key col-sm-3">Application Scaledd</div><div class="value col-sm-9">${model.applicationScale}</div>
            </#if>
            <#if model.keyOutputVariables?has_content>
            <div class="key col-sm-3">Key Output Variablesdd</div><div class="value col-sm-9">${model.keyOutputVariables}</div>
            </#if>
            <#if model.keyInputVariables?has_content>
            <div class="key col-sm-3">Key Input Variablesdd</div><div class="value col-sm-9">${model.keyInputVariables}</div>
            </#if>
            <#if model.temporalResolution?has_content>
            <div class="key col-sm-3">Temporal Resolutiondd</div><div class="value col-sm-9">${model.temporalResolution}</div>
            </#if>
            <#if model.spatialResolution?has_content>
            <div class="key col-sm-3">Spatial Resolutiondd</div><div class="value col-sm-9">${model.spatialResolution}</div>
            </#if>
            <#if model.inputDataAvailableInDataCatalogue?has_content>
            <div class="key col-sm-3">Input Data Available in Data Cataloguedd</div><div class="value col-sm-9">${model.inputDataAvailableInDataCatalogue}</div>
            </#if>
          </div>
          </#list>
        </#if>
        <#if inputData?? >
          <h2>Input Data</h2>
          <ul>
          <#list inputData as input>
            <li>${input}</li>
          </#list>
          </ul>
        </#if>
      </div>
    </div>
</@skeleton.master>
