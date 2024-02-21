a dcmitype:Collection ;
<#list jena.incomingEidcRelations(uri)>
dct:hasPart
  <#items as associatedResource>
    <${associatedResource.href}><#sep>,
  </#items>;
</#list>
