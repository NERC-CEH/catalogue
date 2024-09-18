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
        <@blocks.links jena.modelApplications(uri) "Model uses" />
      </div>
      <div class="col-sm-8 col-sm-pull-4">
        <dl class="dl-horizontal">
          <#if version?has_content>
          <dt>Version</dt><dd>${version}</dd>
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
          <#if releaseDate?has_content>
          <dt>Release Date</dt><dd>${releaseDate}</dd>
          </#if>
          <#if license?has_content>
          <dt>License</dt><dd>${license}</dd>
          </#if>
          <#if operatingRequirements?has_content>
          <dt>Operating Requirements</dt><dd>${operatingRequirements}</dd>
          </#if>
          <#if applicationType?has_content>
          <dt>Application Type</dt><dd>${applicationType}</dd>
          </#if>
          <#if userInterface?has_content>
          <dt>User Interface</dt><dd>${userInterface}</dd>
          </#if>
          <#if supportAvailable?has_content>
          <dt>Support Available</dt><dd>${supportAvailable}</dd>
          </#if>
          <#if applicationScale?has_content>
          <dt>Application Scale</dt><dd>${applicationScale}</dd>
          </#if>
          <#if geographicalRestrictions?has_content>
          <dt>Geographical Restrictions</dt><dd>${geographicalRestrictions}</dd>
          </#if>
          <#if temporalResolution?has_content>
          <dt>Temporal Resolution</dt><dd>${temporalResolution}</dd>
          </#if>
          <#if spatialResolution?has_content>
          <dt>Spatial Resolution</dt><dd>${spatialResolution}</dd>
          </#if>
          <#if primaryPurpose?has_content>
          <dt>Primary Purpose</dt><dd>${primaryPurpose}</dd>
          </#if>
          <#if keyOutputVariables?has_content>
          <dt>Key Output Variables</dt><dd>${keyOutputVariables}</dd>
          </#if>
          <#if keyInputVariables?has_content>
          <dt>Key Input Variables</dt><dd>${keyInputVariables}</dd>
          </#if>
          <#if calibrationRequired?has_content>
          <dt>Calibration Required</dt><dd>${calibrationRequired}</dd>
          </#if>
          <#if modelStructure?has_content>
          <dt>Model Structure</dt><dd>${modelStructure}</dd>
          </#if>
          <#if modelParameterisation?has_content>
          <dt>Model Parameterisation</dt><dd>${modelParameterisation}</dd>
          </#if>
          <#if inputDataAvailableOnCatalogue?has_content>
          <dt>Input Data Available on CaMMP Catalogue</dt><dd>${inputDataAvailableOnCatalogue}</dd>
          </#if>
          <#if (documentation?? && documentation.href??) >
          <dt>Documentation</dt><dd><a href="${documentation.href!}">${documentation.value!documentation.href}</a></dd>
          </#if>
        </dl>

        <#if keyReferences?? >
          <#list keyReferences>
            <h2>Key References</h2>
            <ul>
            <#items as reference>
              <li>${reference}</li>
            </#items>
            </ul>
          </#list>
        </#if>

        <#if inputData?? >
          <#list inputData>
            <h2>Input Data</h2>
            <ul>
            <#items as input>
              <li>${input}</li>
            </#items>
            </ul>
          </#list>
        </#if>

        <#if outputData?? >
          <#list outputData>
            <h2>Output Data</h2>
            <ul>
              <#items as output>
                <li>${output}</li>
              </#items>
            </ul>
          </#list>
        </#if>

        <h2>Quality Assurance</h2>
        <dl class="dl-horizontal">
          <dt>Developer Testing</dt><dd><#if developerTesting?has_content>${developerTesting?then('Yes', 'No')}<#else>Unknown</#if></dd>
          <dt>Internal Peer Review</dt><dd><#if internalPeerReview?has_content>${internalPeerReview?then('Yes', 'No')}<#else>Unknown</#if></dd>
          <dt>External Peer Review</dt><dd><#if externalPeerReview?has_content>${externalPeerReview?then('Yes', 'No')}<#else>Unknown</#if></dd>
          <dt>Use of Version Control</dt><dd><#if useOfVersionControl?has_content>${useOfVersionControl?then('Yes', 'No')}<#else>Unknown</#if></dd>
          <dt>Internal Model Audit</dt><dd><#if internalModelAudit?has_content>${internalModelAudit?then('Yes', 'No')}<#else>Unknown</#if></dd>
          <dt>External Model Audit</dt><dd><#if externalModelAudit?has_content>${externalModelAudit?then('Yes', 'No')}<#else>Unknown</#if></dd>
          <dt>Quality Assurance Guidelines and Checklists</dt><dd><#if qualityAssuranceGuidelinesAndChecklists?has_content>${qualityAssuranceGuidelinesAndChecklists?then('Yes', 'No')}<#else>Unknown</#if></dd>
          <dt>Governance</dt><dd><#if governance?has_content>${governance?then('Yes', 'No')}<#else>Unknown</#if></dd>
          <dt>Transparency</dt><dd><#if transparency?has_content>${transparency?then('Yes', 'No')}<#else>Unknown</#if></dd>
          <dt>Periodic Review</dt><dd><#if periodicReview?has_content>${periodicReview?then('Yes', 'No')}<#else>Unknown</#if></dd>
        </dl>
      </div>
    </div>
  </div>
</@skeleton.master>
