<#import "../functions.ftlh" as func>
<#setting date_format = 'yyyy-MM-dd'>
<#if useConstraints?has_content>
    <#assign licences = func.filter(useConstraints, "code", "license")>
</#if>
<#if responsibleParties?has_content>
    <#assign
    authors = func.filter(responsibleParties, "role", "author")
    pointsOfContact = func.filter(responsibleParties, "role", "pointOfContact")
    publishers  = func.filter(responsibleParties, "role", "publisher")
    >
</#if>

<#macro contactList contacts prefix="c">
  <#if contacts?has_content>
    <#list contacts as contact>
      <#assign contactIdentifier="_:" + prefix + contact?counter>

      <#if contact.individualName?has_content>
        <#if contact.nameIdentifier?has_content && contact.nameIdentifier?matches("^http(|s)://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{3}(X|\\d)$")>
          <#assign contactIdentifier= "\l" + contact.nameIdentifier + "\g">
        </#if>
      <#else>
        <#if contact.organisationIdentifier?has_content && contact.organisationIdentifier?matches("^http(|s)://ror.org/[0-9a-z]+$")>
          <#assign contactIdentifier="\l" + contact.organisationIdentifier+ "\g">
        </#if>
      </#if>

      ${contactIdentifier?no_esc}<#sep>,</#sep>
    </#list>
  </#if>
</#macro>

<#macro contactDetail contacts prefix="c">
  <#if contacts?has_content>
    <#list contacts as contact>
      <#assign contactIdentifier="_:" + prefix + contact?counter>

      <#if contact.individualName?has_content>
        <#assign contactType="vcard:Individual">
        <#assign contactName=contact.individualName>
        <#if contact.organisationName?has_content>
          <#assign orgName=contact.organisationName>
        </#if>
        <#if contact.nameIdentifier?has_content && contact.nameIdentifier?matches("^http(|s)://orcid.org/\\d{4}-\\d{4}-\\d{4}-\\d{3}(X|\\d)$")>
          <#assign contactIdentifier="\l" + contact.nameIdentifier + "\g">
        </#if>
      <#elseif contact.organisationName?has_content >
        <#assign contactType="vcard:Organization">
        <#assign contactName=contact.organisationName>
        <#assign orgName="">
        <#if contact.organisationIdentifier?has_content && contact.organisationIdentifier?matches("^http(|s)://ror.org/[0-9a-z]+$")>
          <#assign contactIdentifier="\l" + contact.organisationIdentifier+ "\g">
        </#if>
      </#if>

      <#--
      UKCEH and EIDC organisations are defined at the catalogue level so don't need to be included here
      Only return data for organisations other than UKCEH or EIDC
      -->
      <#if !contactIdentifier?matches("^\lhttp(|s)://ror.org/04xw4m193\g$") && !contactIdentifier?matches("^\lhttp(|s)://ror.org/00pggkr55\g$")>
        ${contactIdentifier?no_esc} a ${contactType} ;
        vcard:fn "${contactName}" ;
        <#if orgName?has_content>vcard:organization-name "${orgName}" ;</#if>
        <#if contact.email?has_content>vcard:hasEmail "${contact.email}" ;</#if>
        .
      </#if>
    </#list>
  </#if>
</#macro>