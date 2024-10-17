<#ftl output_format="plainText">
<#compress>
<#import "_common.ftl" as c>
<@c.common rdftype="sosa:Platform, ef:Facility" prefixed=prefixed!true>
   <#if geometry?has_content>
    dct:geometry "${geometry.wkt?replace('Optional[','')?replace(']$','','r')}"^^geo:wktLiteral ;
  </#if>

  <#if environmentalDomain??>
    ef:mediaMonitored <#list environmentalDomain as ad><${ad.uri}><#sep>,</#sep></#list> ;
  </#if>
</@c.common>
</#compress>
