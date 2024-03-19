<#ftl output_format="plainText">
<#compress>
<#import "_common.ftl" as c>
<#assign other>
<#-- other statements not about <id> -->
</#assign>
<@c.common other=other>
<#-- network specific statements about <id> -->
  a <https://ukeof.org.uk/network> ;
</@c.common>
</#compress>
