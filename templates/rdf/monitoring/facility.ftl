<#ftl output_format="plainText">
<#compress>
<#import "_common.ftl" as c>
<#assign other>
<https://example.com/p1> a <https://ukeof.org.uk/person> ; <#-- example of other statements not about <id> -->
  dct:title "Alice Brown" .
</#assign>
<@c.common other=other>
  a <https://ukeof.org.uk/facility> ; <#-- an example of Facility specific statements -->
</@c.common>
</#compress>
