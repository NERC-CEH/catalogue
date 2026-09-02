<#setting date_format = 'yyyy-MM-dd'>
<#import "_turtle.ftl" as ttl>

<#macro displayLiteral string>
  <#t>"${ttl.escape(string?trim)}"
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

      <#--
        Whether the node we are about to describe is an externally-governed
        identifier: a ROR for an organisation, an ORCID or ISNI for a person.

        Tested on the emitted node rather than on contact.isRor()/isOrcid(),
        because ContactUri falls back to a minted node when an identifier is
        present but unusable. Only ContactUri knows which way that went, and it
        signals it in the node itself — an <IRI> is external, a prefixed name is
        ours. Re-deriving the precedence here would let the two drift apart.
      -->
      <#local externalNode = contactIdentifier?starts_with("<")>

      <#--
        The affiliation, worked out for every contact: a ROR where the record
        supplies a usable one, otherwise a node minted from the organisation's
        name.

        Not asserted where the contact IS the organisation. contactName and
        organisationName are then the same string, so foaf:member would make the
        organisation a member of a second node bearing its own name — 24 such
        statements in production before dri-one #334.
      -->
      <#local memberRor = "">
      <#local memberOrg = "">
      <#if contactType == "foaf:Person">
        <#if contact.isRor()>
          <#local memberRor = uriNormaliser.normalise(contact.organisationIdentifier)>
        </#if>
        <#local memberOrg = contactUri.identifyOrganisation(contact)>
      </#if>

      <#--
        Identity literals are asserted only on a node we minted. Writing them
        onto an external identifier overwrites a shared node with whatever a
        depositor happened to type, since every record naming it writes to the
        same node and RDF keeps all of it (dri-one #320). Production showed
        exactly that once ORCIDs were included: 281 ORCIDs carrying more than
        one foaf:name, including one researcher's ORCID asserting a different
        person's name (dri-one #348).

        foaf:member stays on both. It is not a claim about who the person is,
        but about what this record says of them, so it belongs with
        pro:holdsRoleInTime rather than with the name. An email address is
        asserted on neither — see the note below.
      -->
      ${contactIdentifier} a ${contactType} ;
        <#if !externalNode>
          foaf:name "${ttl.escape(contactName?trim)}" ;
          <#if contact.familyName?has_content >foaf:familyName "${ttl.escape(contact.familyName?trim)}" ;</#if>
          <#if contact.givenName?has_content >foaf:givenName "${ttl.escape(contact.givenName?trim)}" ;</#if>
        </#if>
        <#if memberRor?has_content>
          foaf:member <${memberRor}> ;
        <#elseif memberOrg?has_content>
          foaf:member ${memberOrg} ;
        </#if>
      .

      <#--
        No vcard:hasEmail, on any contact. The RDF export has no per-role
        context, and the record page already withholds these addresses per role
        — templates/html/dataResource/_contacts.ftlh passes showEmail=false for
        authors — while this export published them for everyone. 5,066 people
        holding the author role had an address in the graph that their own
        record page does not show, 3,693 of the 3,710 distinct addresses being
        individuals' rather than role inboxes (dri-one #348).
      -->

      <#--
        The affiliation that has no ROR. Unlike a ROR — an externally-governed
        identifier that must not be given record text (dri-one #320) — this node
        is minted from the organisation's name, so the name is what identifies
        it and asserting it is safe. Emitted here rather than in a detail macro
        to keep it beside the reference: the same organisation named on several
        contacts writes the same triples, which RDF collapses.
      -->
      <#if !memberRor?has_content && memberOrg?has_content>
${memberOrg} a foaf:Organization ;
  foaf:name <@displayLiteral contact.organisationName /> .
      </#if>

      <#--
        The role statement gets its own node rather than a blank one (dri-one
        #334). A pro:RoleInTime is a reified statement, and reification exists
        so the statement can be pointed at — which is exactly what a blank node
        prevents.
      -->
      <#local doiRole = doiRoleUri(contact)>
      <#if doiRole?has_content>
        <#local roleNode = contactUri.identifyRole(contactIdentifier, doiRole, id)>
        ${contactIdentifier} pro:holdsRoleInTime ${roleNode} .
        ${roleNode} a pro:RoleInTime ;
          pro:withRole ${doiRole} ;
          pro:relatesToEntity :${id} .
      </#if>

    </#list>
  </#if>
</#macro>

<#--
  A distribution's file format, identified by its IANA media type registration
  where the record supplies a media type and by a node minted from its name
  otherwise (FormatUri, dri-one #334). Shared with the dcterms:format list in
  turtle/_dataset.ftl through formatUris, so the two can never disagree about
  which node a format is.

  The media-type node is externally governed and shared with every other DCAT
  publisher, so it gets its type and nothing derived from record text — the
  same rule keywordDetail and organisationRORs follow (dri-one #320). The
  minted node is keyed on the name, so its label is what identifies it and is
  safe to assert.
-->
<#macro formatDetail>
  <#list (distributionFormats![])?filter(f -> formatUris.hasContent(f)) as format>
    <#local mediaType = formatUris.mediaTypeUri(format)>
    <#if mediaType?has_content>
<${mediaType}> a dcterms:IMT .
    <#else>
${formatUris.identify(format)} a dcterms:IMT ;
  rdf:value <@displayLiteral format.name /> ;
  rdfs:label <@displayLiteral format.name /> .
    </#if>
  </#list>
</#macro>

<#--
  A grant is identified by the most trustworthy identifier it carries: a node
  minted from the funder's own awardNumber, or the awardURI where no award
  number was supplied, and otherwise a node scoped to this record (dri-one
  #322, #324). A funding entry with none of awardTitle, awardNumber, awardURI
  or funderIdentifier is suppressed entirely rather than falling back to that
  record-scoped node: it would carry nothing but rdf:type, an empty node
  standing for a grant the record says nothing about (dri-one #322). Shared
  by fundingList and fundingDetail — both filter through fundingUri.hasContent
  so the two can never disagree about which entries are suppressed, nor which
  node a funding entry that survives the filter is.
-->
<#macro fundingList>
  <#list funding?filter(f -> fundingUri.hasContent(f)) as fund>
    ${fundingUri.identify(fund, id, fund?index)}<#sep>,</#sep><#t>
  </#list>
</#macro>

<#macro fundingDetail>
  <#list funding?filter(f -> fundingUri.hasContent(f)) as fund>

      <#local grantIdentifier = fundingUri.identify(fund, id, fund?index)>

      ${grantIdentifier} a frapo:Grant, prov:Activity ;
        <#if fund.awardTitle?has_content>rdfs:label <@displayLiteral fund.awardTitle /> ;</#if>
        <#if fund.awardNumber?has_content>frapo:hasGrantNumber <@displayLiteral fund.awardNumber /> ;</#if>
        <#if fund.awardNumber?has_content && fund.awardURI?has_content>
          <#local awardSameAs = uriNormaliser.normalise(fund.awardURI)>
          <#if awardSameAs?has_content>owl:sameAs <${awardSameAs}> ;</#if>
        </#if>
        frapo:funds :${id} ;
      .

      <#if fund.funderIdentifier?has_content>
        <#local funderUri = uriNormaliser.normalise(fund.funderIdentifier)>
        <#if funderUri?has_content>
          <#--
            No foaf:name from fund.funderName. funderIdentifier is an
            externally-governed shared identifier (a ROR, or a Crossref funder
            DOI), so every record naming that funder writes to the one node:
            "Biotechnology and Biological Sciences Research Council" and
            "BBSRC" both land on ror.org/00cwqg982 and both persist, because
            RDF is set-based. That is the corruption dri-one #320 removed from
            contactDetail and organisationRORs, and it applies here for the
            same reason. Consumers resolve the identifier; where we do assert a
            name for a ROR it comes from the controlled list in
            catalogue.ttl.ftl, not from record text.
          -->
          <${funderUri}> a frapo:FundingAgency ;
            frapo:awards ${grantIdentifier} .
        </#if>
      </#if>
  </#list>
</#macro>

<#--
  A keyword is identified by its concept URI where it has a usable one, by the
  concept its text unambiguously names where it does not (KeywordUri, dri-one
  #321), and by its label otherwise. Shared by keywordList and keywordDetail so
  the two can never disagree about which node a keyword is.
-->
<#macro keywordList keywords>
  <#list keywords as kw>

    <#local kwUri = keywordUri.identify(kw)>
    <#if kwUri?has_content>
      <#assign keyword ="\l" + kwUri + "\g">
    <#else>
      <#assign keyword ='"' + ttl.escape(kw.value!"") + '"'>
    </#if>

    ${keyword}<#sep>,</#sep><#t>
  </#list>
</#macro>

<#--
  The node keywordUri picks is an externally-governed, shared concept (GeoNames,
  GEMET, CEHMD, NVS, ...); kw.value is only ever this record's depositor-typed
  label for it. Asserting that label as the concept's skos:prefLabel/rdfs:label
  would overwrite shared vocabulary data with whatever any one record happened
  to type, typos included (dri-one #320) — so where the keyword resolves to a
  concept, emit only its type and nothing derived from record text. A keyword
  promoted from a literal (dri-one #321) is no different: it is the same shared
  node, reached by its label instead of by a URI on the record.
-->
<#macro keywordDetail keywords>
  <#list keywords as kw>
    <#local kwUri = keywordUri.identify(kw)>
    <#if kwUri?has_content>
      <${kwUri}> a skos:Concept .<#t>
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
          <#assign keyword ='"' + ttl.escape(op.title) + '"'>
        <#else>
          <#assign keyword ='"' + ttl.escape(op.value!"") + '"'>
        </#if>
        ${keyword}<#sep>,</#sep><#t>
      </#list>
    <#sep>,</#sep><#t>
  </#list>
</#macro>

<#--
  Same reasoning as keywordDetail: an observed property's uri identifies a
  shared concept, and op.title/op.value are only ever this record's own text
  for it. See dri-one #320.
-->
<#macro opDetail>
  <#list fileset as filesetOp>
    <#if filesetOp.observedProperty?has_content>
      <#list filesetOp.observedProperty as op>
        <#local opUri = uriNormaliser.normalise(op.uri!"")>
        <#if opUri?has_content>
          <${opUri}> a skos:Concept .
        </#if>
      </#list>
    </#if>
  </#list>
</#macro>

<#--
  dri-one #326: sosa:observedProperty duplicates sdo:variableMeasured, but
  only for observed properties that already carry a uri to a controlled-
  vocabulary concept - choosing/backfilling that vocabulary for the free-text
  ones is deferred to a follow-up issue. Unlike opList's sdo:variableMeasured,
  there is no literal fallback here: an observed property without a usable
  uri contributes nothing to this list, so it never picks up a bogus concept.
-->
<#function observedPropertyUri op>
  <#return uriNormaliser.normalise(op.uri!"")>
</#function>

<#macro opSosaList>
  <#list fileset?filter(fs -> fs.observedProperty?filter(op -> observedPropertyUri(op)?has_content)?has_content) as filesetOp>
    <#list filesetOp.observedProperty?filter(op -> observedPropertyUri(op)?has_content) as op>
      <#assign sosaOp = "\l" + observedPropertyUri(op) + "\g">
      ${sosaOp}<#sep>,</#sep><#t>
    </#list>
    <#sep>,</#sep><#t>
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

<#--
  A licence or access-rights statement with only free text (no URI) is
  identified by a node minted from that text (dri-one #327), so the same
  wording is one node wherever it recurs and can be filtered/compared
  instead of being an unidentifiable blank node. Mirrors the minting done
  inline in _rights.ftl, so the two can never disagree about which node a
  piece of text is.
-->
<#macro rightsDetail>
  <#if licences?has_content>
    <#list licences as licence>
      <#if !(uriNormaliser.normalise(licence.uri!"")?has_content) && licence.value?has_content>
${licenceUris.mintLicence(licence.value)} a dcterms:LicenseDocument ;
  rdfs:label <@displayLiteral licence.value?replace("\n", " ") /> .
      </#if>
    </#list>
  </#if>
  <#if accessLimitation?has_content
    && !(uriNormaliser.normalise(accessLimitation.uri!"")?has_content)
    && accessLimitation.value?has_content>
${licenceUris.mintAccessRights(accessLimitation.value)} a dcterms:RightsStatement ;
  rdfs:label <@displayLiteral accessLimitation.value /> .
  </#if>
  <#--
    The copyright notice each dcterms:rights in turtle/_rights.ftl points at.
    Filtered identically there and here, so neither can describe a node the
    other did not reference. The substitutions are cosmetic — Turtle has no
    escape for a literal newline, and © survives the round trip unreliably —
    and are applied only to the emitted literal: mintCopyright keys on the
    stored text, so how a notice is rendered cannot change which node it is.
  -->
  <#list (copyrights![])?filter(c -> c.value?has_content) as copyright>
${licenceUris.mintCopyright(copyright.value)} a dcterms:RightsStatement ;
  odrs:copyrightNotice <@displayLiteral copyright.value?replace("©","copyright")?replace("\n", " ") /> .
  </#list>
</#macro>

<#--
  authorPointOfContactWithRORs (GeminiDocument#getAuthorPointOfContactWithRORs)
  is one contact per distinct ROR seen among this record's authors/contact
  points, so contact.organisationName here is whichever person's typed
  affiliation happened to be first — not the organisation's own name. Asserting
  it as the ROR's foaf:name is the same corruption keywordDetail/opDetail had:
  a shared, externally-governed node accumulating every string anyone ever
  typed for it, across every record (dri-one #320). Emit only the type.
-->
<#macro organisationRORs>
  <#list authorPointOfContactWithRORs as contact>
    <#local ror = uriNormaliser.normalise(contact.organisationIdentifier!"")>
    <#if ror?has_content>
  <${ror}> a foaf:Organization .
    </#if>
  </#list>
</#macro>