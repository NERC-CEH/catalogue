<#ftl output_format="plainText">
<#compress>
<#import "_common.ftl" as c>
<#assign other>
<https://example.com/1234> a <https://ukeof.org.uk/network> ; <#-- example of other statements not about <id> -->
  dct:title "Example Network 1234" .
</#assign>
<@c.common other=other>
  a <https://ukeof.org.uk/activity> ; <#-- an example of Activity specific statements -->
</@c.common>
</#compress>
