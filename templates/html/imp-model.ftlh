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
        <div class="key-value row">
          <#if version?has_content>
          <div class="key col-sm-3">Version</div><div class="value col-sm-9">${version}</dd>
          </#if>
          <#if contact??>
            <div class="key col-sm-3">Contact</div>
            <div class="value col-sm-9">
              <#if contact.individualName?has_content>${contact.individualName}</#if><#if contact.organisationName?has_content>, ${contact.organisationName}</#if>
            </dd>
          </#if>
          <#if keywords??>
          <div class="key col-sm-3">Keywords</div><div class="value col-sm-9"><@blocks.keywords keywords/></div>
          </#if>
          <#if releaseDate?has_content>
          <div class="key col-sm-3">Release Date</div><div class="value col-sm-9">${releaseDate}</div>
          </#if>
          <#if license?has_content>
          <div class="key col-sm-3">License</div><div class="value col-sm-9">${license}</div>
          </#if>
          <#if operatingRequirements?has_content>
          <div class="key col-sm-3">Operating Requirements</div><div class="value col-sm-9">${operatingRequirements}</div>
          </#if>
          <#if applicationType?has_content>
          <div class="key col-sm-3">Application Type</div><div class="value col-sm-9">${applicationType}</div>
          </#if>
          <#if userInterface?has_content>
          <div class="key col-sm-3">User Interface</div><div class="value col-sm-9">${userInterface}</div>
          </#if>
          <#if supportAvailable?has_content>
          <div class="key col-sm-3">Support Available</div><div class="value col-sm-9">${supportAvailable}</div>
          </#if>
          <#if applicationScale?has_content>
          <div class="key col-sm-3">Application Scale</div><div class="value col-sm-9">${applicationScale}</div>
          </#if>
          <#if geographicalRestrictions?has_content>
          <div class="key col-sm-3">Geographical Restrictions</div><div class="value col-sm-9">${geographicalRestrictions}</div>
          </#if>
          <#if temporalResolution?has_content>
          <div class="key col-sm-3">Temporal Resolution</div><div class="value col-sm-9">${temporalResolution}</div>
          </#if>
          <#if spatialResolution?has_content>
          <div class="key col-sm-3">Spatial Resolution</div><div class="value col-sm-9">${spatialResolution}</div>
          </#if>
          <#if primaryPurpose?has_content>
          <div class="key col-sm-3">Primary Purpose</div><div class="value col-sm-9">${primaryPurpose}</div>
          </#if>
          <#if keyOutputVariables?has_content>
          <div class="key col-sm-3">Key Output Variables</div><div class="value col-sm-9">${keyOutputVariables}</div>
          </#if>
          <#if keyInputVariables?has_content>
          <div class="key col-sm-3">Key Input Variables</div><div class="value col-sm-9">${keyInputVariables}</div>
          </#if>
          <#if calibrationRequired?has_content>
          <div class="key col-sm-3">Calibration Required</div><div class="value col-sm-9">${calibrationRequired}</div>
          </#if>
          <#if modelStructure?has_content>
          <div class="key col-sm-3">Model Structure</div><div class="value col-sm-9">${modelStructure}</div>
          </#if>
          <#if modelParameterisation?has_content>
          <div class="key col-sm-3">Model Parameterisation</div><div class="value col-sm-9">${modelParameterisation}</div>
          </#if>
          <#if inputDataAvailableOnCatalogue?has_content>
          <div class="key col-sm-3">Input Data Available on CaMMP Catalogue</div><div class="value col-sm-9">${inputDataAvailableOnCatalogue}</div>
          </#if>
          <#if (documentation?? && documentation.href??) >
          <div class="key col-sm-3">Documentation</div><div class="value col-sm-9"><a href="${documentation.href!}">${documentation.value!documentation.href}</a></div>
          </#if>
        </div>

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
        <div class="key-value row">
          <div class="key col-sm-3">Developer Testing</div><div class="value col-sm-9"><#if developerTesting?has_content>${developerTesting?then('Yes', 'No')}<#else>Unknown</#if></div>
          <div class="key col-sm-3">Internal Peer Review</div><div class="value col-sm-9"><#if internalPeerReview?has_content>${internalPeerReview?then('Yes', 'No')}<#else>Unknown</#if></div>
          <div class="key col-sm-3">External Peer Review</div><div class="value col-sm-9"><#if externalPeerReview?has_content>${externalPeerReview?then('Yes', 'No')}<#else>Unknown</#if></div>
          <div class="key col-sm-3">Use of Version Control</div><div class="value col-sm-9"><#if useOfVersionControl?has_content>${useOfVersionControl?then('Yes', 'No')}<#else>Unknown</#if></div>
          <div class="key col-sm-3">Internal Model Audit</div><div class="value col-sm-9"><#if internalModelAudit?has_content>${internalModelAudit?then('Yes', 'No')}<#else>Unknown</#if></div>
          <div class="key col-sm-3">External Model Audit</div><div class="value col-sm-9"><#if externalModelAudit?has_content>${externalModelAudit?then('Yes', 'No')}<#else>Unknown</#if></div>
          <div class="key col-sm-3">Quality Assurance Guidelines and Checklists</div><div class="value col-sm-9"><#if qualityAssuranceGuidelinesAndChecklists?has_content>${qualityAssuranceGuidelinesAndChecklists?then('Yes', 'No')}<#else>Unknown</#if></div>
          <div class="key col-sm-3">Governance</div><div class="value col-sm-9"><#if governance?has_content>${governance?then('Yes', 'No')}<#else>Unknown</#if></div>
          <div class="key col-sm-3">Transparency</div><div class="value col-sm-9"><#if transparency?has_content>${transparency?then('Yes', 'No')}<#else>Unknown</#if></div>
          <div class="key col-sm-3">Periodic Review</div><div class="value col-sm-9"><#if periodicReview?has_content>${periodicReview?then('Yes', 'No')}<#else>Unknown</#if></div>
        </div>
      </div>
    </div>
  </div>
</@skeleton.master>
