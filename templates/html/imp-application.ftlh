<#import "blocks.ftlh" as blocks>
<#import "skeleton.ftlh" as skeleton>
<#import "../underscore.ftlh" as _>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)>
  <div id="metadata" class="container">

    <@blocks.title title type />
    <@blocks.description description!"" />
    <div class="row">
      <div class="col-sm-4 col-sm-push-8">
        <#include "imp/_admin.ftlh">
        <@blocks.links jena.datasets(uri) "Datasets" />
        <@blocks.links jena.models(uri) "Models" />
      </div>
      <div class="col-sm-8 col-sm-pull-4">
        <dl class="dl-horizontal">
          <#if relevanceToCaMMP?has_content>
          <dt>Relevance to CaMMP</dt><dd>${relevanceToCaMMP}</dd>
          </#if>
          <#if date?has_content>
          <dt>Date</dt><dd>${date}</dd>
          </#if>
          <#if studySite?has_content>
          <dt>Study Site</dt><dd>${studySite}</dd>
          </#if>
          <#if objective?has_content>
          <dt>Objective</dt><dd>${objective}</dd>
          </#if>
          <#if funderDetails?has_content>
          <dt>Funder Details</dt><dd>${funderDetails}</dd>
          </#if>
          <#if contact??>
            <dt>Contact</dt>
            <dd>
              <#if contact.individualName?has_content>${contact.individualName}</#if><#if contact.organisationName?has_content>, ${contact.organisationName}</#if>
            </dd>
          </#if>
          <#if keywords??>
          <dt>Keywords</dt><dd><@blocks.keywords keywords/></dd>
          </#if>
          <#if multipleModelsUsed?has_content>
          <dt>Multiple Models Used</dt><dd>${multipleModelsUsed}</dd>
          </#if>
          <#if multipleModelLinkages?has_content>
          <dt>Multiple Model Linkages</dt><dd>${multipleModelLinkages}</dd>
          </#if>
          <#if sensitivity?has_content>
          <dt>Sensitivity</dt><dd>${sensitivity}</dd>
          </#if>
          <#if uncertainty?has_content>
          <dt>Uncertainty</dt><dd>${uncertainty}</dd>
          </#if>
          <#if validation?has_content>
          <dt>Validation</dt><dd>${validation}</dd>
          </#if>
          <#if modelEasyToUse?has_content>
          <dt>Model Easy To Use</dt><dd>${modelEasyToUse}</dd>
          </#if>
          <#if userManualUseful?has_content>
          <dt>User Manual Useful</dt><dd>${userManualUseful}</dd>
          </#if>
          <#if dataObtainable?has_content>
          <dt>Data Obtainable</dt><dd>${dataObtainable}</dd>
          </#if>
          <#if modelUnderstandable?has_content>
          <dt>Model Understandable</dt><dd>${modelUnderstandable}</dd>
          </#if>
        </dl>
        <#if models?? >
          <h2>Models</h2>
          <#list models as model>
          <dl class="dl-horizontal">
            <#if model.name?has_content>
            <dt>Name</dt><dd>${model.name}</dd>
            </#if>
            <#if model.version?has_content>
            <dt>Version</dt><dd>${model.version}</dd>
            </#if>
            <#if model.primaryPurpose?has_content>
            <dt>Primary Purpose</dt><dd>${model.primaryPurpose}</dd>
            </#if>
            <#if model.applicationScale?has_content>
            <dt>Application Scale</dt><dd>${model.applicationScale}</dd>
            </#if>
            <#if model.keyOutputVariables?has_content>
            <dt>Key Output Variables</dt><dd>${model.keyOutputVariables}</dd>
            </#if>
            <#if model.keyInputVariables?has_content>
            <dt>Key Input Variables</dt><dd>${model.keyInputVariables}</dd>
            </#if>
            <#if model.temporalResolution?has_content>
            <dt>Temporal Resolution</dt><dd>${model.temporalResolution}</dd>
            </#if>
            <#if model.spatialResolution?has_content>
            <dt>Spatial Resolution</dt><dd>${model.spatialResolution}</dd>
            </#if>
            <#if model.inputDataAvailableInDataCatalogue?has_content>
            <dt>Input Data Available in Data Catalogue</dt><dd>${model.inputDataAvailableInDataCatalogue}</dd>
            </#if>
          </dl>
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
