<#import "skeleton.html.tpl" as skeleton>
<#import "blocks.html.tpl" as b>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)><#escape x as x?html>
  <@b.metadataContainer "ceh-model">
    <@b.sectionHeading>Basic Info</@b.sectionHeading>
    <@b.key "Model name" "Name of the model">${title}</@b.key>
    <@b.key "Primary purpose" "Short phrase to describe primary aim of model">
      <#noescape>
        <@b.linebreaks primaryPurpose />
      </#noescape>
    </@b.key>
    <@b.key "Website" "Link to outward facing model website if one exists"><@b.bareUrl website /></@b.key>
    <@b.key "Senior Responsible Officer" "Senior responsible officer for the model (this should be the person who is the primary contact for the model)">
      <#if seniorResponsibleOfficer?? && seniorResponsibleOfficer?has_content>
        ${seniorResponsibleOfficer}
      </#if>
      <#if seniorResponsibleOfficerEmail?? && seniorResponsibleOfficerEmail?has_content>
        (${seniorResponsibleOfficerEmail})
      </#if>
    </@b.key>
    <@b.key "Organisations involved" "Organisations involved in model development"><@b.bareList organisations /></@b.key>
    <@b.key "Keywords" "Keywords for model discovery"><@b.keywords keywords/></@b.key>
    <@b.key "License" "License type (open or non-open) under which the model is distributed">${licenseType?cap_first}</@b.key>
    <@b.sectionHeading>References</@b.sectionHeading>
    <@b.references references />
    <@b.sectionHeading>Model Description</@b.sectionHeading>
    <@b.key "Key input variables" "Short phrases to describe basic types of model inputs"><@b.bareList keyInputVariables /></@b.key>
    <@b.key "Key output variables" "Short phrases to describe basic types of model outputs"><@b.bareList keyOutputVariables /></@b.key>
    <@b.key "Model description" "Longer description of model e.g. development history, use to answer science questions, overview of structure">
      <#noescape>
        <@b.linebreaks description />
      </#noescape>
    </@b.key>
    <@b.key "Model type" "Type which best fits model">${modelType?cap_first}</@b.key>
    <@b.key "Current model version" "Most recent release version">
      ${currentModelVersion}
      <#if releaseDate?? && releaseDate?has_content>
      (${releaseDate?date})
      </#if>
    </@b.key>
    <@b.key "Model calibration" "Does the model need calibration before running? If so, what needs to be supplied to do this?">${modelCalibration}</@b.key>
    <@b.sectionHeading>Spatio-Temporal Info</@b.sectionHeading>
    <@b.key "Spatial domain" "Is the model only applicable to certain areas?">${spatialDomain}</@b.key>
    <@b.key "Spatial resolution" "Spatial resolution at which model works or at which model outputs are generated">${spatialResolution}</@b.key>
    <@b.key "Temporal resolution (min)" "Minimum time step supported by the model">${temporalResolutionMin}</@b.key>
    <@b.key "Temporal resolution (max)" "Maximum time step supported by the model ">${temporalResolutionMax}</@b.key>
    <@b.sectionHeading>Technical Info</@b.sectionHeading>
    <@b.key "Language" "Computing language in which the model is written">${language}</@b.key>
    <@b.key "Compiler" "Compiled required">${compiler}</@b.key>
    <@b.key "Operating system" "Operating system typically used to run the model">${operatingSystem}</@b.key>
    <@b.key "System memory" "Memory required to run code">${systemMemory}</@b.key>
    <@b.key "Documentation" "Location of technical documentation for the model"><@b.bareUrl documentation /></@b.key>
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
    <@b.sectionHeading>Version control change notes</@b.sectionHeading>
    <@b.versionHistories versionHistories />
    <@b.sectionHeading>Project usage</@b.sectionHeading>
    <@b.projectUsages projectUsages />
  </@b.metadataContainer>
</#escape></@skeleton.master>