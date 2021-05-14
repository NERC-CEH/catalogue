a dcmitype:Software ;
<#include "_rights.ftlh">

<#--Authors-->
<#if authors?has_content>
  dct:contributor
  <#include "_authors.ftlh">
  ;
</#if>