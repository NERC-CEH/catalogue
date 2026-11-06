<#import "_catalogRecord.ftl" as cr>
:${id}
  dcterms:title <@displayLiteral title /> ;

  <@canonicalIdentifier resourceIdentifiers />
  <@otherIdentifiers resourceIdentifiers />

  <#if datacitable?string=='true' && citation?has_content>
      <#assign citationString =  citation.authors?join(', ') + " (" + citation.year?string("0") +"). " + citation.title + ". " + citation.publisher + ". " + citation.url?trim>
      dcterms:bibliographicCitation <@displayLiteral citationString/> ;
  </#if>

  <#if availability != "Deleted">
    <#if description?has_content>
      dcterms:description <@displayLiteral description /> ;
    </#if>

    <#if lineage?has_content>
      dcterms:provenance [
        a dcterms:ProvenanceStatement ;
        rdfs:label <@displayLiteral lineage />
      ] ;
    </#if>

    <#list boundingBoxes as extent>
     dcterms:spatial [
        a dcterms:Location ;
        dcat:bbox "${ttl.escape(extent.wkt)}"^^geo:wktLiteral ;
      ] ;
    </#list>

    <#if temporalExtents?has_content>
      <@temporal />
    </#if>

    <#--Points of contact-->
    <#if contactPoints?has_content>
      dcat:contactPoint <@contactList contactPoints "c" />  ;
    </#if>

    <#--Publisher-->
    <#if publishers?has_content>
      dcterms:publisher <@contactList publishers "pub" /> ;
    </#if>

    <#--Contributors: the one list the editor offers a contributorRole on (dri-one #405)-->
    <#if contributors?has_content>
      dcterms:contributor <@contactList contributors "con" /> ;
    </#if>

    <#--Relationships-->
    <#-- dri-one #327: a target that is itself withdrawn/unpublished (available in the Jena
         index, but not to the outside world) must not be linked to as if it resolved. -->
    <#list jena.relationships(uri, "http://purl.org/dc/terms/isPartOf")?filter(item -> item.availability != "Deleted")>
      dcterms:isPartOf <#items as item><${item.href}><#sep>, </#items> ;
    </#list>
    <#list jena.relationships(uri, "http://purl.org/dc/terms/replaces")?filter(item -> item.availability != "Deleted")>
      dcterms:replaces <#items as item><${item.href}><#sep>, </#items> ;
    </#list>

    <#list jena.relationships(uri, "http://purl.org/dc/terms/relation")?filter(item -> item.availability != "Deleted")>
      dcterms:relation <#items as item><${item.href}><#sep>, </#items> ;
    </#list>
    <#list jena.relationships(uri, "https://digital.ceh.ac.uk/ontology/doo/utilises")?filter(item -> item.availability != "Deleted")>
      doo:utilises <#items as item><${item.href}><#sep>, </#items> ;
    </#list>
    <#-- requires / isRequiredBy / isReplacedBy name only published, non-withdrawn records: the
         inverse lookups return incoming nodes whatever their publication state, so a draft that
         requires or replaces this record must not be named in the public export.
         dcterms:source and the CEHMD supersedes relationship are deliberately not exported: no
         public exported record holds either (only the CodeDocument and InfrastructureRecord
         editors offer them, types that are not exported, dri-one #403), and that editor labels
         dcterms:source "Produces", the inverse of Dublin Core's meaning. -->
    <#list jena.relationships(uri, "http://purl.org/dc/terms/requires")?filter(item -> item.availability != "Deleted" && item.publicationStatus == "published")>
      dcterms:requires <#items as item><${item.href}><#sep>, </#items> ;
    </#list>
    <#-- The inverse links read the incoming statement; the same guards apply, so a public record
         never names a withdrawn or unpublished one as its dependant or successor. -->
    <#list jena.inverseRelationships(uri, "http://purl.org/dc/terms/requires")?filter(item -> item.availability != "Deleted" && item.publicationStatus == "published")>
      dcterms:isRequiredBy <#items as item><${item.href}><#sep>, </#items> ;
    </#list>
    <#list jena.inverseRelationships(uri, "http://purl.org/dc/terms/replaces")?filter(item -> item.availability != "Deleted" && item.publicationStatus == "published")>
      dcterms:isReplacedBy <#items as item><${item.href}><#sep>, </#items> ;
    </#list>

    <#--Citations-->
    <#if incomingCitations?has_content>
      dcterms:isReferencedBy <@incomingCitationList /> ;
    </#if>

    <#if allKeywords?has_content>
      dcterms:subject <@keywordList allKeywords/> ;
    </#if>

    <#--
      Controlled-vocabulary links. Each list uses the <#list ...><#items> form (see
      turtle/_rights.ftl) so entries filtered out as blank emit no predicate. Only
      links are emitted: nothing the record says (SRS title, theme label, topic
      category value) is attached to the external IRIs.
      DOO's spatial representation individuals are an allow-list, matched
      case-insensitively and written in DOO's case; anything else is omitted.
    -->
    <#assign dooSpatialRepresentations = ["grid", "stereoModel", "textTable", "tin", "vector", "video"]>
    <#assign dooSpatialRepresentationIris = []>
    <#list (spatialRepresentationTypes![])?filter(t -> t?has_content) as srt>
      <#list dooSpatialRepresentations?filter(d -> d?lower_case == srt?trim?lower_case) as match>
        <#assign dooSpatialRepresentationIris = dooSpatialRepresentationIris + ["doo:" + match]>
      </#list>
    </#list>
    <#if dooSpatialRepresentationIris?has_content>
      doo:spatialRepresentationType ${dooSpatialRepresentationIris?join(", ")} ;
    </#if>

    <#list (spatialReferenceSystems![])?filter(s -> crsUris.canonical(s)?has_content)>
      dcterms:conformsTo <#t>
      <#items as srs>
        <${crsUris.canonical(srs)}><#sep>, </#sep>
      </#items> ;
    </#list>

    <#assign themeUris = ((inspireThemes![])?filter(t -> uriNormaliser.normalise(t.uri!"")?has_content)?map(t -> uriNormaliser.normalise(t.uri))
      + (topicCategories![])?filter(t -> uriNormaliser.normalise(t.uri!"")?has_content)?map(t -> uriNormaliser.normalise(t.uri)))>
    <#list themeUris>
      dcat:theme <#t>
      <#items as themeUri>
        <${themeUri}><#sep>, </#sep>
      </#items> ;
    </#list>

    <#list (resourceMaintenance![])?filter(m -> frequencyUris.uri(m.frequencyOfUpdate!"")?has_content)>
      dcterms:accrualPeriodicity <#t>
      <#items as maintenance>
        <${frequencyUris.uri(maintenance.frequencyOfUpdate)}><#sep>, </#sep>
      </#items> ;
    </#list>

    <#if fileset?? && fileset?has_content && fileset?filter(fs -> fs.observedProperty?has_content)?has_content>
      sdo:variableMeasured <@opList /> ;
    </#if>

    <#if fileset?? && fileset?has_content && fileset?filter(fs -> fs.observedProperty?filter(op -> observedPropertyUri(op)?has_content)?has_content)?has_content>
      sosa:observedProperty <@opSosaList /> ;
    </#if>

    <#if funding?filter(f -> fundingUri.hasContent(f))?has_content>
    prov:wasGeneratedBy <@fundingList /> ;
    </#if>

    <#--
      Dates, once for every type. publicationDate is when the record (and its DOI)
      went public: dcterms:issued. releasedDate is when an embargo lifted and the data
      became downloadable: dcterms:available, falling back to publicationDate. A future
      releasedDate is kept as a future dcterms:available. supersededDate, deprecatedDate
      and unavailableDate are deliberately not exported.
    -->
    <#if datasetReferenceDate??>
      <#if datasetReferenceDate.publicationDate?has_content>
        dcterms:issued "${datasetReferenceDate.publicationDate}"^^xsd:date ;
      </#if>
      <#if datasetReferenceDate.releasedDate?has_content>
        dcterms:available "${datasetReferenceDate.releasedDate}"^^xsd:date ;
      <#elseif datasetReferenceDate.publicationDate?has_content>
        dcterms:available "${datasetReferenceDate.publicationDate}"^^xsd:date ;
      </#if>
      <#if datasetReferenceDate.creationDate?has_content>
        dcterms:created "${datasetReferenceDate.creationDate}"^^xsd:date ;
      </#if>
      <#if datasetReferenceDate.revisionDate?has_content>
        dcterms:modified "${datasetReferenceDate.revisionDate}"^^xsd:date ;
      </#if>
    </#if>

    <#-- A free string (controller ruling R3): any stored version is emitted as is; ?c stops
         3.6 being localised to 3,6. -->
    <#if version??>
      dcat:version "${version?c}" ;
    </#if>

    <#--
      emitsRights tracks whether the type-specific include below pulls in
      turtle/_rights.ftl, so that rightsDetail only describes a minted licence or
      access-rights node when something actually references it. Set alongside each
      branch rather than as a second list of type names, so the two cannot drift.
    -->
    <#assign emitsRights = false>
    <#assign emitsFormats = false>
    <#if type=='dataset' || type=='nonGeographicDataset' || type=='signpost'>
      <#assign emitsRights = true>
      <#include "turtle/_dataset.ftl">
    <#elseif type=='aggregate'|| type=='collection'|| type=='series'>
      <#include "turtle/_aggregation.ftl"> <#--no rights block-->
    <#elseif type=='service'>
      <#assign emitsRights = true>
      <#include "turtle/_service.ftl">
    <#elseif type=='software' || type=='model'|| type=='computationalNotebook'>
      <#assign emitsRights = true>
      <#include "turtle/_code.ftl">
    </#if>

    dcterms:language <http://id.loc.gov/vocabulary/iso639-1/en> . <#-- leave here to close all the statements about the dataset -->

    <#if contactPoints?has_content>
      <@contactDetail contactPoints "c" />
    </#if>

    <#if publishers?has_content>
      <@contactDetail publishers "pub" />
    </#if>

    <#if authors?has_content>
      <@contactDetail authors "a" />
    </#if>

    <#if contributors?has_content>
      <@contactDetail contributors "con" />
    </#if>

    <#--
      Other contacts only where their role maps onto DOO — in practice a
      principal investigator (dri-one #405). The rest (mediator, stakeholder,
      user…) have no confirmed equivalent and no predicate linking them to the
      record, so describing them would leave nodes nothing points at.
    -->
    <#assign mappedOtherContacts = (otherContacts![])?filter(c -> doiRoleUri(c)?has_content)>
    <#if mappedOtherContacts?has_content>
      <@contactDetail mappedOtherContacts "oc" />
    </#if>

    <#if incomingCitations?has_content>
      <@incomingCitationDetail />
    </#if>

    <#if allKeywords?has_content>
      <@keywordDetail allKeywords />
    </#if>

    <#if fileset?? && fileset?has_content>
      <@opDetail />
    </#if>

    <#if authorPointOfContactWithRORs?has_content>
      <@organisationRORs />
    </#if>

    <#if emitsRights>
      <@rightsDetail />
    </#if>

    <#if emitsFormats>
      <@formatDetail />
    </#if>

    <@fundingDetail />
  <#else>
    dcterms:description "This resource is no longer available please contact the Environmental Information Data Centre for more details" ;
    .
  </#if>

  <@cr.catalogRecord />

  <#macro canonicalIdentifier resourceIdentifiers>

    <#local domain = uri?replace(id, "")>
    <#local canonicalId = resourceIdentifiers?filter(id -> id.code?starts_with(domain))?first!>

    <#if canonicalId?has_content>
      dcterms:identifier <${canonicalId.code}> ;
    </#if>

  </#macro>

  <#macro otherIdentifiers resourceIdentifiers>

    <#local domain = uri?replace(id, "")>
    <#local dois = resourceIdentifiers?filter(id -> id.codeSpace?matches("doi"))!>
    <#local otherIds = resourceIdentifiers?filter(id -> !id.code?starts_with(domain) && !(id.codeSpace?? && id.codeSpace?matches("doi")) )>

    <#if dois?has_content>

      <#list dois>
        adms:identifier <#t>
          <#items as id>
            <https://doi.org/${id.code}><#t>
          <#sep>,</#sep><#t>
          </#items> ;<#t>
      </#list>

    </#if>

    <#if otherIds?has_content>

      <#list otherIds>
        adms:identifier <#t>
          <#items as id>
            "<#if id.codeSpace?? && id.codeSpace?has_content && !id.codeSpace?starts_with("http")>${ttl.escape(id.codeSpace)}/</#if>${ttl.escape(id.code)}"<#t>
          <#sep>,</#sep><#t>
          </#items> ;<#t>
      </#list>

    </#if>

  </#macro>


  <#macro idSameAs resourceIdentifiers>

    <#local domain = uri?replace(id, "")>
    <#local otherIds = resourceIdentifiers?filter(id -> !id.code?starts_with(domain) && !(id.codeSpace?? && id.codeSpace?matches("doi")) )>

    <#if otherIds?has_content>

      <#list otherIds>
        adms:identifier <#t>
          <#items as id>
            "<#if id.codeSpace?? && id.codeSpace?has_content && !id.codeSpace?starts_with("http")>${ttl.escape(id.codeSpace)}/</#if>${ttl.escape(id.code)}"<#t>
          <#sep>,</#sep><#t>
          </#items> ;<#t>
      </#list>

    </#if>

  </#macro>
