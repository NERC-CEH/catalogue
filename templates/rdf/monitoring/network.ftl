<#ftl output_format="plainText">
<#compress>
<#import "_common.ftl" as c>
<#assign other>
<https://example.com/org5> a <https://ukeof.org.uk/organisation> ; <#-- example of other statements not about <id> -->
  dct:title "University of Lancaster" .
</#assign>
<@c.common other=other>
  a <https://ukeof.org.uk/network> ; <#-- an example of Network specific statements -->
</@c.common>
</#compress>
