a dcmitype:Software ;
<#include "_rights.ftl">

<#--Authors-->
<#if describable(authors)?has_content>
  dcterms:creator <@contactList authors "a" />  ;
</#if>
