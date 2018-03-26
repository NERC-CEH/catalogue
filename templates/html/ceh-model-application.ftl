<#import "skeleton.ftl" as skeleton>
<#import "blocks.ftl" as b>

<@skeleton.master title=title catalogue=catalogues.retrieve(metadata.catalogue)><#escape x as x?html>
  <@b.metadataContainer "ceh-model">
    <@b.admin />
    <#if title?? || projectObjectives?? || description?? || keywords?? || projectCompletionDate?? || projectWebsite?? || funderDetails?? || contactName?? || multipleModelsUsed?? || multipleModelLinkages??>
      <@b.sectionHeading>Project Information</@b.sectionHeading>
      <#if title?? && title?has_content>
        <@b.key "Project title" "Title of project">${title}</@b.key>
      </#if>
      <#if projectObjectives?? && projectObjectives?has_content>
        <@b.key "Project objectives" "Brief description of main objectives">
          <#noescape>
            <@b.linebreaks projectObjectives />
          </#noescape>
        </@b.key>
      </#if>
      <#if description?? && description?has_content>
        <@b.key "Project description" "Longer description of project including why models were used to answer the science question, assumptions made, key outputs">
          <#noescape>
            <@b.linebreaks description />
          </#noescape>
        </@b.key>
      </#if>
      <#if keywords?? && keywords?has_content>
        <@b.key "Keywords" "Keywords to enable searching for the project"><@b.keywords keywords/></@b.key>
      </#if>
      <#if projectCompletionDate?? && projectCompletionDate?has_content>
        <@b.key "Project completion date" "Project end date">${projectCompletionDate}</@b.key>
      </#if>
      <#if projectWebsite?? && projectWebsite?has_content>
        <@b.key "Project website" "Link to public-facing website if available"><@b.bareUrl projectWebsite /></@b.key>
      </#if>
      <#if funderDetails?? && funderDetails?has_content>
        <@b.key "Funder details" "Funder details including grant number if appropriate">
          <#noescape>
            <@b.linebreaks funderDetails />
          </#noescape>
        </@b.key>
      </#if>
      <#if contactName??>
        <@b.key "Contact name" "Name of CEH PI/project representative">
          <#if contactName?has_content>
            ${contactName}
          </#if>
          <#if contactEmail?? && contactEmail?has_content>
            (${contactEmail})
          </#if>
        </@b.key>
      </#if>
      <#if multipleModelsUsed?? && multipleModelsUsed?has_content>
        <@b.key "Multiple models used?" "Were multiple models used in the project? If so, which ones?">
          <#noescape>
            <@b.linebreaks multipleModelsUsed />
          </#noescape>
        </@b.key>
      </#if>
      <#if multipleModelLinkages?? && multipleModelLinkages?has_content>
        <@b.key "Multiple model linkages" "If multiple models were used how was this done e.g. chained, independent runs, comparisons, ensemble">
          <#noescape>
            <@b.linebreaks multipleModelLinkages />
          </#noescape>
        </@b.key>
      </#if>
    </#if>
    <#if references?? && references?has_content>
      <@b.sectionHeading>References</@b.sectionHeading>
      <#list references as ref>
        <@b.reference ref />
      </#list>
    </#if>
    <#if modelInfos?? && modelInfos?has_content>
      <@b.sectionHeading>Model Information</@b.sectionHeading>
      <#list modelInfos as modelInfo>
        <@b.modelInfo modelInfo />
      </#list>
    </#if>
    <#if inputData?? || outputData??>
      <@b.sectionHeading>Data Information</@b.sectionHeading>
      <#if inputData?? && inputData?has_content>
        <@b.key "Input data" "Detailed description of input data including: variable name, units, file format, URL to data catalogue record for each input">
            <#list inputData as input>
              <@b.dataInfo input/>
            </#list>
        </@b.key>
      </#if>
      <#if outputData?? && outputData?has_content>
        <@b.key "Output data" "Detailed description of model outputs incling: variable name, units, file format, URL to data catalogue record for each output (or alternative location of model outputs from this application)">
            <#list inputData as input>
              <@b.dataInfo input/>
            </#list>
        </@b.key>
      </#if>
    </#if>
    <#if sensitivityAnalysis?? || uncertaintyAnalysis?? || validation??>
      <@b.sectionHeading>Evaluation Information</@b.sectionHeading>
      <#if sensitivityAnalysis?? && sensitivityAnalysis?has_content>
        <@b.key "Sensitivity analysis" "Details of any sensitivity analysis performed, or link to appropriate documentation">
          <#noescape>
            <@b.linebreaks sensitivityAnalysis />
          </#noescape>
        </@b.key>
      </#if>
      <#if uncertaintyAnalysis?? && uncertaintyAnalysis?has_content>
        <@b.key "Uncertainty analysis" "How was uncertainty in the model captured and represented? Give links to any appropriate documentation">
          <#noescape>
            <@b.linebreaks uncertaintyAnalysis />
          </#noescape>
        </@b.key>
      </#if>
      <#if validation?? && validation?has_content>
        <@b.key "Uncertainty analysis" "Was the model validated against data not used for model building or parameterisation? If so, provide links to any documentation of results">
          <#noescape>
            <@b.linebreaks validation />
          </#noescape>
        </@b.key>
      </#if>
    </#if>
    <#if metadataDate?? && metadataDate?has_content>
      <@b.sectionHeading>Metadata</@b.sectionHeading>
      <@b.key "Metadata Date" "Date metadata last updated">${metadataDateTime}</@b.key>
    </#if>
  </@b.metadataContainer>
</#escape></@skeleton.master>