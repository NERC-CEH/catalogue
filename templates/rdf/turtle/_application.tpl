a dcmitype:Software ;
<#include "_rights.tpl">

<#--Authors-->
<#if authors?has_content>
  dct:contributor
  <#include "_authors.tpl">
  ;
</#if>