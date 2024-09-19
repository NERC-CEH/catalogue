<#ftl output_format="plainText">
<#compress>
<#include "_macros.ftl">
<#include "_prefixes.ftl">
PREFIX : <${uri?replace(id,"")}>
<#include "_body.ftl">
</#compress>
