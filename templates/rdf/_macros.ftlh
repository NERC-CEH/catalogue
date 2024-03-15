<#import "../functions.ftlh" as func>
<#setting date_format = 'yyyy-MM-dd'>
<#if useConstraints?has_content>
    <#assign licences = func.filter(useConstraints, "code", "license")>
</#if>

<#macro contactList contacts prefix="c">
  <#if contacts?has_content>
    <#list contacts as contact>

      <#assign contactIdentifier= "<" + id + "_" + prefix +  contact?index + ">">
      <#if contact.individualName?has_content>
        <#if contact.isOrcid()>
          <#assign contactIdentifier= "\l" + contact.nameIdentifier?trim + "\g">
        </#if>
      <#else>
        <#if contact.isRor()>
          <#assign contactIdentifier="\l" + contact.organisationIdentifier?trim + "\g">
        </#if>
      </#if>

      ${contactIdentifier?no_esc}<#sep>,</#sep>
    </#list>
  </#if>
</#macro>

<#macro contactDetail contacts prefix="c">
  <#if contacts?has_content>
    <#list contacts as contact>
      <#if contact.individualName?has_content || contact.organisationIdentifier?has_content>
        <#assign contactIdentifier= "<" + id + "_" + prefix + contact?index + ">">
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


          <#--
          UKCEH and EIDC organisations are defined at the catalogue level so don't need to be included here
          Only return data for organisations other than UKCEH or EIDC
          -->
          <#if !contactIdentifier?matches("^\lhttp(|s)://ror.org/04xw4m193\g$") && !contactIdentifier?matches("^\lhttp(|s)://ror.org/00pggkr55\g$")>
            ${contactIdentifier?no_esc} a ${contactType} ;
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

      <#assign fundIdentifier= id + "_fund" + fund?index>
      <#if fund.awardURI?has_content>
        <#assign fundIdentifier=fund.awardURI>
      </#if>
      <${fundIdentifier?trim?no_esc}><#sep>,</#sep>
    </#list>
  </#if>
</#macro>

<#macro fundingDetail>
  <#if funding?has_content>
    <#list funding as fund>

      <#assign fundIdentifier= id + "_proj" + fund?index>
      <#if fund.awardURI?has_content>
        <#assign fundIdentifier=fund.awardURI>
      </#if>

      <${fundIdentifier?trim?no_esc}> a prov:Activity ; <#if fund.awardTitle?has_content>rdfs:label "${fund.awardTitle}"</#if> .
    </#list>
  </#if>
</#macro>

<#macro keywordList keywords>
  <#list keywords as kw>
    <#if kw.uri?has_content>
      <#assign keyword ="\l" + kw.uri?trim+ "\g">
    <#else>
      <#assign keyword ='"' + kw.value+ '"'>
    </#if>
    ${keyword?no_esc}<#sep>,</#sep>
  </#list>
</#macro>

<#macro keywordDetail keywords>
  <#list keywords as kw>
    <#if kw.uri?has_content>
      <${kw.uri?trim}> a skos:Concept; skos:prefLabel "${kw.value}"; rdfs:label "${kw.value}".
    </#if>
  </#list>
</#macro>

<#--ELTER -->
  <#macro projectList>
    <#if elterProject?has_content>
      <#list elterProject as project>

        <#assign projectIdentifier= id + "_proj" + project?index>
        <#if project.uri?has_content>
          <#assign projectIdentifier=project.uri?trim>
        </#if>
        <${projectIdentifier?no_esc}><#sep>,</#sep>
      </#list>
    </#if>
  </#macro>

  <#macro projectDetail>
    <#if elterProject?has_content>
      <#list elterProject as project>

        <#assign projectIdentifier= id + "_proj" + project?index>
        <#if project.uri?has_content>
          <#assign projectIdentifier=project.uri?trim>
        </#if>

        <${projectIdentifier?no_esc}> a prov:Activity ; <#if project.value?has_content>rdfs:label "${project.value}"</#if> .
      </#list>
    </#if>
  </#macro>

  <#macro deimsList>
    <#if deimsSites?has_content>
      <#list deimsSites as deimsSite>

        <#assign deimsID= id + "_site" + deimsSite?index>
        <#if deimsSite.url?has_content>
          <#assign deimsID=deimsSite.url>
        </#if>
        <${deimsID?no_esc}><#sep>,</#sep>
      </#list>
    </#if>
  </#macro>

  <#macro deimsDetail>
    <#if deimsSites?has_content>
      <#list deimsSites as deimsSite>

        <#assign deimsID= id + "_site" + deimsSite?index>
        <#if deimsSite.url?has_content>
          <#assign deimsID=deimsSite.url>
        </#if>

        <${deimsID?no_esc}> a prov:Location; <#if deimsSite.title?has_content>rdfs:label "${deimsSite.title}"</#if> .
      </#list>
    </#if>
  </#macro>
<#--END ELTER-->
