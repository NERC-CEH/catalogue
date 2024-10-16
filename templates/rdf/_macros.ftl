<#import "../functions.ftlh" as func>
<#setting date_format = 'yyyy-MM-dd'>
<#if useConstraints?has_content>
    <#assign licences = func.filter(useConstraints, "code", "license")>
</#if>

<#macro displayLiteral string>
  <#--Ensure literals do not contain " characters-->
  <#t>${string?replace("\"","'")?replace("\n"," ")}
</#macro>

<#macro contactList contacts prefix="c">
  <#if contacts?has_content>
    <#list contacts as contact>

      <#assign contactIdentifier= ":" + id + "_" + prefix +  contact?index>
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
        <#assign contactIdentifier= ":" + id + "_" + prefix + contact?index >
        <#if contact.individualName?has_content>
          <#assign contactType="vcard:Individual">
          <#assign contactName=contact.individualName>
          <#if contact.organisationName?has_content>
            <#assign orgName=contact.organisationName>
          </#if>
           <#if contact.isOrcid()>
            <#assign contactIdentifier="\l" + contact.nameIdentifier?trim + "\g">
          </#if>
        <#elseif contact.organisationName?has_content >
          <#assign contactType="vcard:Organization">
          <#assign contactName=contact.organisationName>
          <#assign orgName="">
           <#if contact.isRor()>
            <#assign contactIdentifier="\l" + contact.organisationIdentifier?trim + "\g">
          </#if>
        </#if>
          <#if !contactIdentifier?matches("^\lhttp(|s)://ror.org/04xw4m193\g$") && !contactIdentifier?matches("^\lhttp(|s)://ror.org/00pggkr55\g$")>
            ${contactIdentifier} a ${contactType} ;
            vcard:fn "${contactName?trim}" ;
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

      <#assign fundIdentifier= ":" + id + "_fund" + fund?index>
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

      <#assign fundIdentifier= ":" + id + "_proj" + fund?index>
      <#if fund.awardURI?has_content>
        <#assign fundIdentifier ="\l" + fund.awardURI?trim+ "\g">
      </#if>

      ${fundIdentifier?trim} a prov:Activity ; <#if fund.awardTitle?has_content>rdfs:label "<@displayLiteral fund.awardTitle />"</#if> .
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
      <${kw.uri?trim}> a skos:Concept; skos:prefLabel "<@displayLiteral kw.value />"; rdfs:label "<@displayLiteral kw.value />".
    </#if>
  </#list>
</#macro>

<#macro incomingCitationList>
  <#if incomingCitations?has_content>
    <#list incomingCitations as citation>

      <#assign citationIdentifier= ":" + id + "_citation" + citation?index>
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

      <#assign citationIdentifier= ":" + id + "_citation" + citation?index>
      <#if citation.url?has_content>
        <#assign citationIdentifier ="\l" + citation.url?trim + "\g">
      </#if>

      ${citationIdentifier?trim} a <http://purl.org/vocab/frbr/core#Work> ;
        <#if citation.description?has_content>rdfs:label "<@displayLiteral citation.description />"; </#if>
        .
    </#list>
  </#if>
</#macro>
