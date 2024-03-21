a dcmitype:Software ;
<#include "_rights.ftl">

<#--Authors-->
<#if authors?has_content>
  dct:contributor <@contactList authors "a" />  ;
</#if>
