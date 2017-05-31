<#import "skeleton.html.tpl" as skeleton>
<#import "blocks.html.tpl" as b>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)><#escape x as x?html>
  <@b.metadataContainer "ceh-model">
    <@b.admin />
    <#if title?? || primaryPurpose?? || website?? || seniorResponsibleOfficer?? || organisations?? || keywords?? || licenseType??>
      <@b.sectionHeading>Basic Info</@b.sectionHeading>
      <#if title?? && title?has_content>
        <@b.key "Model name" "Name of the model">${title}</@b.key>
      </#if>
      <#if primaryPurpose?? && primaryPurpose?has_content>
        <@b.key "Primary purpose" "Short phrase to describe primary aim of model">
          <#noescape>
            <@b.linebreaks primaryPurpose />
          </#noescape>
        </@b.key>
      </#if>
      <#if website?? && website?has_content>
        <@b.key "Website" "Link to outward facing model website if one exists"><@b.bareUrl website /></@b.key>
      </#if>
      <#if seniorResponsibleOfficer??>
        <@b.key "Senior Responsible Officer" "Senior responsible officer for the model (this should be the person who is the primary contact for the model)">
          <#if seniorResponsibleOfficer?has_content>
            ${seniorResponsibleOfficer}
          </#if>
          <#if seniorResponsibleOfficerEmail?? && seniorResponsibleOfficerEmail?has_content>
            (${seniorResponsibleOfficerEmail})
          </#if>
        </@b.key>
      </#if>
      <#if organisations?? && organisations?has_content>
        <@b.key "Organisations involved" "Organisations involved in model development"><@b.bareList organisations /></@b.key>
      </#if>
      <#if keywords?? && keywords?has_content>
        <@b.key "Keywords" "Keywords for model discovery"><@b.keywords keywords/></@b.key>
      </#if>
      <#if licenseType?? && licenseType?has_content>
        <@b.key "License" "License type (open or non-open) under which the model is distributed">${licenseType?cap_first}</@b.key>
      </#if>
    </#if>
    <#if references?? && references?has_content>
      <@b.sectionHeading>References</@b.sectionHeading>
      <#list references as ref>
        <@b.reference ref />
      </#list>
    </#if>
    <#if keyInputVariables?? || keyOutputVariables?? || description?? || modelType?? || currentModelVersion?? || modelCalibration??>
      <@b.sectionHeading>Model Description</@b.sectionHeading>
      <#if keyInputVariables?? && keyInputVariables?has_content>
        <@b.key "Key input variables" "Short phrases to describe basic types of model inputs"><@b.bareList keyInputVariables /></@b.key>
      </#if>
      <#if keyOutputVariables?? && keyOutputVariables?has_content>
        <@b.key "Key output variables" "Short phrases to describe basic types of model outputs"><@b.bareList keyOutputVariables /></@b.key>
      </#if>
      <#if description?? && description?has_content>
        <@b.key "Model description" "Longer description of model e.g. development history, use to answer science questions, overview of structure">
          <#noescape>
            <@b.linebreaks description />
          </#noescape>
        </@b.key>
      </#if>
      <#if modelType?? && modelType?has_content>
        <@b.key "Model type" "Type which best fits model">${modelType?cap_first}</@b.key>
      </#if>
      <#if currentModelVersion?? && currentModelVersion?has_content>
        <@b.key "Current model version" "Most recent release version">
          ${currentModelVersion}
          <#if releaseDate?? && releaseDate?has_content>
          (${releaseDate?date})
          </#if>
        </@b.key>
      </#if>
      <#if modelCalibration?? && modelCalibration?has_content>
        <@b.key "Model calibration" "Does the model need calibration before running? If so, what needs to be supplied to do this?">${modelCalibration}</@b.key>
      </#if>
    </#if>
    <#if spatialDomain?? || spatialResolution?? || temporalResolutionMin?? || temporalResolutionMax??>
      <@b.sectionHeading>Spatio-Temporal Info</@b.sectionHeading>
      <#if spatialDomain?? && spatialDomain?has_content>
        <@b.key "Spatial domain" "Is the model only applicable to certain areas?">${spatialDomain}</@b.key>
      </#if>
      <#if spatialResolution?? && spatialResolution?has_content>
        <@b.key "Spatial resolution" "Spatial resolution at which model works or at which model outputs are generated">${spatialResolution}</@b.key>
      </#if>
      <#if temporalResolutionMin?? && temporalResolutionMin?has_content>
        <@b.key "Temporal resolution (min)" "Minimum time step supported by the model">${temporalResolutionMin}</@b.key>
      </#if>
      <#if temporalResolutionMax?? && temporalResolutionMax?has_content>
        <@b.key "Temporal resolution (max)" "Maximum time step supported by the model ">${temporalResolutionMax}</@b.key>
      </#if>
    </#if>
    <#if language?? || compiler?? || operatingSystem?? || systemMemory?? || documentation??>
      <@b.sectionHeading>Technical Info</@b.sectionHeading>
      <#if language?? && language?has_content>
        <@b.key "Language" "Computing language in which the model is written">${language}</@b.key>
      </#if>
      <#if compiler?? && compiler?has_content>
        <@b.key "Compiler" "Compiled required">${compiler}</@b.key>
      </#if>
      <#if operatingSystem?? && operatingSystem?has_content>
        <@b.key "Operating system" "Operating system typically used to run the model">${operatingSystem}</@b.key>
      </#if>
      <#if systemMemory?? && systemMemory?has_content>
        <@b.key "System memory" "Memory required to run code">${systemMemory}</@b.key>
      </#if>
      <#if documentation?? && documentation?has_content>
        <@b.key "Documentation" "Location of technical documentation for the model"><@b.bareUrl documentation /></@b.key>
      </#if>
    </#if>
    <@b.sectionHeading>QA Info</@b.sectionHeading>
    <@b.key "Developer testing" "Use of a range of developer tools including parallel build and analytical review or sense check"><@b.qa developerTesting /></@b.key>
    <@b.key "Internal peer review" "Obtaining a critical evaluation from a third party independent of the development of the model but from within the same organisation"><@b.qa internalPeerReview /></@b.key>
    <@b.key "External peer review" "Formal or informal engagement of a third party to conduct critical evaluation from outside the organisation in which the model is being developed"><@b.qa externalPeerReview /></@b.key>
    <@b.key "Internal model audit" "Formal audit of a model within the organisation, perhaps involving use of internal audit functions"><@b.qa internalModelAudit /></@b.key>
    <@b.key "External model audit" "Formal engagement of external professional to conduct a critical evaluation of the model, perhaps involving audit professionals;"><@b.qa externalModelAudit /></@b.key>
    <@b.key "Quality assurance guidelines and checklists" "Model development refers to department’s guidance or other documented QA processes (e.g. third party publications)"><@b.qa qaGuidelinesAndChecklists /></@b.key>
    <@b.key "Governance" "At least one of planning, design and/or sign-off of model for use is referred to a more senior person.  There is a clear line of accountability for the model"><@b.qa governance /></@b.key>
    <@b.key "Transparency" "Model is placed in the wider domain for scrutiny, and/or results are published"><@b.qa transparency /></@b.key>
    <@b.key "Periodic review" "Model is reviewed at intervals to ensure it remains fit for the intended purpose, if used on an ongoing basis"><@b.qa periodicReview /></@b.key>
    <#if versionHistories?? && versionHistories?has_content>
      <@b.sectionHeading>Version control change notes</@b.sectionHeading>
      <#list versionHistories as history>
        <@b.versionHistory history /> 
      </#list>
    </#if>
    <#if projectUsages?? && projectUsages?has_content>
      <@b.sectionHeading>Project usage</@b.sectionHeading>
      <#list projectUsages as usage>
        <@b.projectUsage usage />    
      </#list>
    </#if>
    <#if metadataDate?? && metadataDate?has_content>
      <@b.sectionHeading>Metadata</@b.sectionHeading>
      <@b.key "Metadata Date" "Date metadata last updated">${metadataDateTime}</@b.key>
    </#if>
  </@b.metadataContainer>
</#escape></@skeleton.master>