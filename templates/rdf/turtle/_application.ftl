a dcmitype:Software ;
<#include "_rights.ftl">

<#--Authors-->
<#if authors?has_content>
  dct:creator <@contactList authors "a" />  ;
</#if>
