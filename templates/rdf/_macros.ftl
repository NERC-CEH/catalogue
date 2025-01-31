<#setting date_format = 'yyyy-MM-dd'>

<#macro displayLiteral string>
  <#--Ensure literals do not contain " characters or line breaks-->
  <#t>"${string?trim?replace("\"","'")?replace("\n"," ")}"
</#macro>

<#macro contactList contacts prefix="c">
  <#if contacts?has_content>
    <#list contacts as contact>

      <#assign contactIdentifier= "_:" + prefix +  contact?index>
      <#if contact.individualName?has_content>
        <#if contact.isOrcid()>
          <#assign contactIdentifier= "\l" + contact.nameIdentifier?trim + "\g">
        </#if>
      <#else>
        <#if contact.isRor()>
          <#assign contactIdentifier="\l" + contact.organisationIdentifier?trim + "\g">
        </#if>
      </#if>

      ${contactIdentifier}<#sep>,</#sep><#t>
    </#list>
  </#if>
</#macro>

<#macro contactDetail contacts prefix="c">
  <#if contacts?has_content>
    <#list contacts as contact>
      <#if contact.individualName?has_content || contact.organisationIdentifier?has_content>
        <#assign contactIdentifier= "_:" + prefix + contact?index >
        <#if contact.individualName?has_content>
          <#assign contactType="foaf:Person">
          <#assign contactName=contact.individualName>
          <#if contact.organisationName?has_content>
            <#assign orgName=contact.organisationName>
          </#if>
           <#if contact.isOrcid()>
            <#assign contactIdentifier="\l" + contact.nameIdentifier?trim + "\g">
          </#if>
        <#elseif contact.organisationName?has_content >
          <#assign contactType="foaf:Organization">
          <#assign contactName=contact.organisationName>
          <#assign orgName="">
           <#if contact.isRor()>
            <#assign contactIdentifier="\l" + contact.organisationIdentifier?trim + "\g">
          </#if>
        </#if>
          <#if !contactIdentifier?matches("^\lhttp(|s)://ror.org/04xw4m193\g$") && !contactIdentifier?matches("^\lhttp(|s)://ror.org/00pggkr55\g$")>
            ${contactIdentifier} a ${contactType} ;
            foaf:name "${contactName?trim}" ;
            <#if orgName?has_content>vcard:organization-name "${orgName?trim}" ;</#if>
            <#if contact.email?has_content>vcard:hasEmail "${contact.email?trim}" ;</#if>
            .
          </#if>
      </#if>
    </#list>
  </#if>
</#macro>

<#macro fundingList>
  <#if funding?has_content>
    <#list funding as fund>

      <#assign fundIdentifier= "_:fund" + fund?index>
      <#if fund.awardURI?has_content>
        <#assign fundIdentifier ="\l" + fund.awardURI?trim+ "\g">
      </#if>
      ${fundIdentifier?trim}<#sep>,</#sep><#t>
    </#list>
  </#if>
</#macro>

<#macro fundingDetail>
  <#if  funding?has_content>
    <#list funding as fund>

      <#assign fundIdentifier= "_:proj" + fund?index>
      <#if fund.awardURI?has_content>
        <#assign fundIdentifier ="\l" + fund.awardURI?trim+ "\g">
      </#if>

      ${fundIdentifier?trim} a prov:Activity ; <#if fund.awardTitle?has_content>rdfs:label <@displayLiteral fund.awardTitle /></#if> .
    </#list>
  </#if>
</#macro>

<#macro keywordList keywords>
  <#list keywords as kw>

    <#if kw.uri?has_content>
      <#assign keyword ="\l" + kw.uri?trim+ "\g">
    <#else>
      <#assign keyword ='"' + kw.value?replace("\"", "") + '"'>
    </#if>

    ${keyword}<#sep>,</#sep><#t>
  </#list>
</#macro>

<#macro keywordDetail keywords>
  <#list keywords as kw>
    <#if kw.uri?has_content>
      <${kw.uri?trim}> a skos:Concept;
        <#if kw.value?has_content >
          skos:prefLabel <@displayLiteral kw.value />; rdfs:label <@displayLiteral kw.value />
        </#if>
        .<#t>
    </#if>
  </#list>
</#macro>

<#macro opList >
  <#list observedProperty as op>
    <#if op.uri?has_content>
      <#assign keyword ="\l" + op.uri?trim+ "\g">
    <#elseif op.title?has_content>
      <#assign keyword ='"' + op.title?replace("\"", "") + '"'>
    <#else>
      <#assign keyword ='"' + op.value?replace("\"", "") + '"'>
    </#if>
    ${keyword}<#sep>,</#sep><#t>
  </#list>
</#macro>

<#macro opDetail>
  <#list observedProperty as op>
    <#assign opLabel = "unknown">
    <#if op.title?has_content>
      <#assign opLabel = op.title>
    <#elseif op.value?has_content>
      <#assign opLabel = op.value>
    </#if>

    <#if op.uri?has_content>
      <${op.uri?trim}> a skos:Concept;skos:prefLabel <@displayLiteral opLabel />; rdfs:label <@displayLiteral opLabel />.
    </#if>
  </#list>
</#macro>

<#macro incomingCitationList>
  <#if incomingCitations?has_content>
    <#list incomingCitations as citation>

      <#assign citationIdentifier= "_:citation" + citation?index>
      <#if citation.url?has_content>
        <#assign citationIdentifier ="\l" + citation.url?trim + "\g">
      </#if>
      ${citationIdentifier?trim}<#sep>,</#sep><#t>
    </#list>
  </#if>
</#macro>

<#macro incomingCitationDetail>
  <#if incomingCitations?has_content>
    <#list incomingCitations as citation>

      <#assign citationIdentifier= "_:citation" + citation?index>
      <#if citation.url?has_content>
        <#assign citationIdentifier ="\l" + citation.url?trim + "\g">
      </#if>

      ${citationIdentifier?trim} a <http://purl.org/vocab/frbr/core#Work> ;
        <#if citation.description?has_content>rdfs:label <@displayLiteral citation.description />; </#if>
        .
    </#list>
  </#if>
</#macro>

<#macro temporal>
  <#list temporalExtents as extent>
    <#if extent.begin?has_content || extent.end?has_content>
      dct:temporal
        [ a dct:PeriodOfTime ;
          <#if extent.begin?has_content>
            dcat:startDate "${extent.begin?date}"^^xsd:date ;
          </#if>
          <#if extent.end?has_content>
            dcat:endDate "${extent.end?date}"^^xsd:date ;
          </#if>
        ] ;
    </#if>
  </#list>
</#macro>
