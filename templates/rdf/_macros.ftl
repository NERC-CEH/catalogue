<#setting date_format = 'yyyy-MM-dd'>

<#macro displayLiteral string>
  <#--Ensure literals do not contain " characters or line breaks-->
  <#t>"${string?trim?replace("\"","'")?replace("\n"," ")}"
</#macro>

<#--
  A contact is identified by the most trustworthy identifier it carries: an ORCID
  or ISNI for a person, a ROR for an organisation, and otherwise a node minted
  from the person's own name, so the same researcher is one node across every
  record they appear on (dri-one #319). Shared by contactList and contactDetail
  so the two can never disagree about which node a contact is.
-->
<#macro contactList contacts prefix="c">
  <#if contacts?has_content>
    <#list contacts as contact>
      ${contactUri.identify(contact, id, prefix, contact?index)}<#sep>,</#sep><#t>
    </#list>
  </#if>
</#macro>

<#--
  A contact's role, mapped onto the Digital Objects Ontology's pro:RoleInTime
  pattern (dri-one #323). Additive: dcterms:creator, dcat:contactPoint and
  dcterms:publisher stay exactly as they are, so DataCite/DOI consumers and
  DCAT tooling keep working. This only adds the detail needed to ask who
  curated a dataset, who led it, or who the field technician was.

  contributorRole is the editor's fixed six-value vocabulary and takes
  precedence; role is a second, much broader controlled list where only
  a handful of values have a confirmed DOO equivalent. Everything else is
  left unmapped rather than guessed.
-->
<#function doiRoleUri contact>
  <#if contact.contributorRole == "dataCreator"><#return "scoro:data-creator"></#if>
  <#if contact.contributorRole == "dataCurator"><#return "scoro:data-curator"></#if>
  <#if contact.contributorRole == "collaborator"><#return "scoro:collaborator"></#if>
  <#if contact.contributorRole == "researcher"><#return "scoro:researcher"></#if>
  <#if contact.contributorRole == "technician"><#return "scoro:technician"></#if>
  <#if contact.contributorRole == "projectLeader"><#return "scoro:project-leader"></#if>
  <#if contact.role == "author"><#return "pro:author"></#if>
  <#if contact.role == "principalInvestigator"><#return "scoro:principal-investigator"></#if>
  <#return "">
</#function>

<#macro contactDetail contacts prefix="c">
  <#if contacts?has_content>
    <#list contacts as contact>

      <#local contactIdentifier = contactUri.identify(contact, id, prefix, contact?index)>

      <#if contact.fullName?has_content>
        <#local contactType="foaf:Person">
        <#local contactName=contact.fullName>
      <#elseif contact.organisationName?has_content >
        <#local contactType="foaf:Organization">
        <#local contactName=contact.organisationName>
      </#if>

      ${contactIdentifier} a ${contactType} ;
        foaf:name "${contactName?trim}" ;
        <#if contact.familyName?has_content >foaf:familyName "${contact.familyName?trim}" ;</#if>
        <#if contact.givenName?has_content >foaf:givenName "${contact.givenName?trim}" ;</#if>
        <#if contact.email?has_content>vcard:hasEmail "${contact.email?trim}" ;</#if>

        <#local memberRor = "">
        <#if contact.isRor()>
          <#local memberRor = uriNormaliser.normalise(contact.organisationIdentifier)>
        </#if>
        <#if memberRor?has_content>
          foaf:member <${memberRor}> ;
        <#elseif contact.organisationName?has_content>
          foaf:member [foaf:name <@displayLiteral contact.organisationName />] ;
        </#if>
      .

      <#local doiRole = doiRoleUri(contact)>
      <#if doiRole?has_content>
        ${contactIdentifier} pro:holdsRoleInTime [
          a pro:RoleInTime ;
          pro:withRole ${doiRole} ;
          pro:relatesToEntity :${id}
        ] .
      </#if>

    </#list>
  </#if>
</#macro>

<#macro fundingList>
  <#if funding?has_content>
    <#list funding as fund>

      <#assign fundIdentifier= ":" + id + "_fund" + fund?index>
      <#if fund.awardURI?has_content>
        <#local awardUri = uriNormaliser.normalise(fund.awardURI)>
        <#if awardUri?has_content>
          <#assign fundIdentifier ="\l" + awardUri + "\g">
        </#if>
      </#if>
      ${fundIdentifier?trim}<#sep>,</#sep><#t>
    </#list>
  </#if>
</#macro>

<#macro fundingDetail>
  <#if  funding?has_content>
    <#list funding as fund>

      <#assign fundIdentifier= ":" + id + "_fund" + fund?index>
      <#if fund.awardURI?has_content>
        <#local awardUri = uriNormaliser.normalise(fund.awardURI)>
        <#if awardUri?has_content>
          <#assign fundIdentifier ="\l" + awardUri + "\g">
        </#if>
      </#if>

      ${fundIdentifier?trim} a prov:Activity ; <#if fund.awardTitle?has_content>rdfs:label <@displayLiteral fund.awardTitle /></#if> .
    </#list>
  </#if>
</#macro>

<#--
  A keyword is identified by its concept URI where it has a usable one, and by
  its label otherwise. Shared by keywordList and keywordDetail so the two can
  never disagree about which node a keyword is.
-->
<#function keywordUri kw>
  <#return uriNormaliser.normalise(kw.uri!"")>
</#function>

<#macro keywordList keywords>
  <#list keywords as kw>

    <#local kwUri = keywordUri(kw)>
    <#if kwUri?has_content>
      <#assign keyword ="\l" + kwUri + "\g">
    <#else>
      <#assign keyword ='"' + (kw.value!"")?replace("\"", "") + '"'>
    </#if>

    ${keyword}<#sep>,</#sep><#t>
  </#list>
</#macro>

<#macro keywordDetail keywords>
  <#list keywords as kw>
    <#local kwUri = keywordUri(kw)>
    <#if kwUri?has_content>
      <${kwUri}> a skos:Concept;
        <#if kw.value?has_content >
          skos:prefLabel <@displayLiteral kw.value />; rdfs:label <@displayLiteral kw.value />
        </#if>
        .<#t>
    </#if>
  </#list>
</#macro>

<#macro opList >
  <#list fileset?filter(fs -> fs.observedProperty?has_content) as filesetOp>
    <#list filesetOp.observedProperty as op>
        <#local opUri = uriNormaliser.normalise(op.uri!"")>
        <#if opUri?has_content>
          <#assign keyword ="\l" + opUri + "\g">
        <#elseif op.title?has_content>
          <#assign keyword ='"' + op.title?replace("\"", "") + '"'>
        <#else>
          <#assign keyword ='"' + (op.value!"")?replace("\"", "") + '"'>
        </#if>
        ${keyword}<#sep>,</#sep><#t>
      </#list>
    <#sep>,</#sep><#t>
  </#list>
</#macro>

<#macro opDetail>
  <#list fileset as filesetOp>
    <#if filesetOp.observedProperty?has_content>
      <#list filesetOp.observedProperty as op>
        <#assign opLabel = "unknown">
        <#if op.title?has_content>
          <#assign opLabel = op.title>
        <#elseif op.value?has_content>
          <#assign opLabel = op.value>
        </#if>

        <#local opUri = uriNormaliser.normalise(op.uri!"")>
        <#if opUri?has_content>
          <${opUri}> a skos:Concept;skos:prefLabel <@displayLiteral opLabel />; rdfs:label <@displayLiteral opLabel />.
        </#if>
      </#list>
    </#if>
  </#list>
</#macro>

<#macro incomingCitationList>
  <#if incomingCitations?has_content>
    <#list incomingCitations as citation>

      <#assign citationIdentifier= ":" + id + "_citation" + citation?index>
      <#if citation.url?has_content>
        <#local citationUri = uriNormaliser.normalise(citation.url)>
        <#if citationUri?has_content>
          <#assign citationIdentifier ="\l" + citationUri + "\g">
        </#if>
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
        <#local citationUri = uriNormaliser.normalise(citation.url)>
        <#if citationUri?has_content>
          <#assign citationIdentifier ="\l" + citationUri + "\g">
        </#if>
      </#if>

      ${citationIdentifier?trim} a <http://purl.org/spar/fabio/Expression> ;
        <#if citation.description?has_content>dcterms:bibliographicCitation <@displayLiteral citation.description?replace("–","-")?replace("…","...") />; </#if>
        .
    </#list>
  </#if>
</#macro>

<#macro temporal>
  <#list temporalExtents as extent>
    <#if extent.begin?has_content || extent.end?has_content>
      dcterms:temporal
        [ a dcterms:PeriodOfTime ;
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

<#macro organisationRORs>
  <#list authorPointOfContactWithRORs as contact>
    <#local ror = uriNormaliser.normalise(contact.organisationIdentifier!"")>
    <#if ror?has_content>
  <${ror}> a foaf:Organization ;
    foaf:name "${contact.organisationName}" .
    </#if>
  </#list>
</#macro>