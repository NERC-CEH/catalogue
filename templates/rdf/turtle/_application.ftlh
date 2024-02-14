a dcmitype:Software ;
<#include "_rights.ftlh">

<#--Authors-->
<#if authors?has_content>
  dct:contributor <@contactList authors "a" />  ;
</#if>
