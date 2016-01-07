<#import "blocks.html.tpl" as blocks>
<#import "skeleton.html.tpl" as skeleton>
<#import "../underscore.tpl" as _>

<@skeleton.master title=title>
<#escape x as x?html>
  <div id="metadata" class="container">

    <@blocks.title title type />
    <@blocks.description description />
    <dl class="dl-horizontal">
      <#if version?has_content>
      <dt>Version</dt><dd>${version}</dd>
      </#if>
      <#if contact?has_content>
      <dt>Contact</dt><dd>${contact}</dd>
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
      <#if smallestAndLargestApplication?has_content>
      <dt>Samllest and Largest Application</dt><dd>${smallestAndLargestApplication}</dd>
      </#if>
      <#if geographicalRestrictions?has_content>
      <dt>Geographical Restrictions</dt><dd>${geographicalRestrictions}</dd>
      </#if>
      <#if temporalResolution?has_content>
      <dt>Temporal Resolution</dt><dd>${temporalResolution}</dd>
      </#if>
      <#if keyOutputs?has_content>
      <dt>Key Outputs</dt><dd>${keyOutputs}</dd>
      </#if>
      <#if calibrationRequired?has_content>
      <dt>Calibration Required</dt><dd>${calibrationRequired}</dd>
      </#if>
      <#if modelStructure?has_content>
      <dt>Model Structure</dt><dd>${modelStructure}</dd>
      </#if>
      <#if dataInput?has_content>
      <dt>Data Input</dt><dd>${dataInput}</dd>
      </#if>
      <#if (documentation?? && documentation.href??) >
      <dt>Documentation</dt><dd><a href="${documentation.href!}">${documentation.value!documentation.href}</a></dd>
      </#if>
    </dl>

    <#if keyReferences?? >
      <#list keyReferences>
        <h3>Key References</h3>
        <ul>
        <#items as reference>
          <li>${reference}</li>
        </#items>
        </ul>
      </#list>
    </#if>

    <#if inputData?? >
      <#list inputData>
        <h3>Input Data</h3>
        <ul>
        <#items as input>
          <li>${input}</li>
        </#items>
        </ul>
      </#list>
    </#if>

    <#if outputData?? >
      <#list outputData>
        <h3>Output Data</h3>
        <ul>
          <#items as output>
            <li>${output}</li>
          </#items>
        </ul>
      </#list>
    </#if>
    
    <h3>Quality Assurance</h3>
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
</#escape>
</@skeleton.master>
