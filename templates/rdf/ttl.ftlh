<#ftl encoding="UTF-8">
<#compress>
<#include "_macros.ftlh">
<#include "_prefixes.ftlh">
@base <${uri?replace(id,"")?replace("id/","")}> .
@prefix : <${uri?replace(id,"")}> .
<#include "_body.ftlh">
</#compress>
