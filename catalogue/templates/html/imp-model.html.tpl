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
      <h3>Key References</h3>
      <ul>
      <#list keyReferences as reference>
        <li>${reference}</li>
      </#list>
      </ul>
    </#if>

    <#if inputData?? >
      <h3>Input Data</h3>
      <ul>
      <#list inputData as input>
        <li>${input}</li>
      </#list>
      </ul>
    </#if>

    <#if outputData?? >
      <h3>Output Data</h3>
      <ul>
      <#list outputData as output>
        <li>${output}</li>
      </#list>
      </ul>
    </#if>

  </div>
</#escape>
</@skeleton.master>
