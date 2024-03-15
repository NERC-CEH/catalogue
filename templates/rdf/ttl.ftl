<#ftl output_format="plainText">
<#compress>
<#include "_macros.ftl">
<#include "_prefixes.ftl">
@base <${uri?replace(id,"")?replace("id/","")}> .
@prefix : <${uri?replace(id,"")}> .
<#include "_body.ftl">
</#compress>
