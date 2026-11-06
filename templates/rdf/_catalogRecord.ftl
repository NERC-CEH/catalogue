<#ftl output_format="plainText">
<#--
  The catalogue record node shared by every record template: when this metadata was last
  changed, as distinct from the resource it describes. Full IRIs, so it does not depend on
  which prefixes the including template declares; only ":" (the record namespace) is assumed.
  A metadataDateTime that is not a valid xsd:dateTime is omitted rather than coerced.
-->
<#macro catalogRecord>
  <#local modified = (metadataDateTime!"")>
  :${id}_record
    a <http://www.w3.org/ns/dcat#CatalogRecord> ;
    <http://xmlns.com/foaf/0.1/primaryTopic> :${id} ;
    <#if modified?matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?")>
      <http://purl.org/dc/terms/modified> "${modified}"^^<http://www.w3.org/2001/XMLSchema#dateTime> ;
    </#if>
    .
</#macro>
