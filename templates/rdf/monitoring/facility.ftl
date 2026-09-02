<#ftl output_format="plainText">
<#compress>
<#import "_common.ftl" as c>
<#import "../_turtle.ftl" as ttl>
<@c.common rdftype="sosa:Platform, doo:EnvironmentalMonitoringFacility" prefixed=prefixed!true>

  <#if !locationConfidential>
    <#if geometry?has_content>
      dcterms:geometry "${ttl.escape(geometry.wkt?replace('Optional[','')?replace(']$','','r'))}"^^geo:wktLiteral ;
    </#if>
  </#if>

  <#if environmentalDomain??>
    <#assign monitoredMedia = environmentalDomain?filter(ad -> uriNormaliser.normalise(ad.uri!"")?has_content)>
    <#if monitoredMedia?has_content>
    ef:mediaMonitored <#list monitoredMedia as ad><${uriNormaliser.normalise(ad.uri)}><#sep>,</#sep></#list> ;
    </#if>
  </#if>
</@c.common>
</#compress>
