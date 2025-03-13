a dcmitype:Collection ;
<#list jena.incomingEidcRelations(uri)>
  dcterms:hasPart <#items as associatedResource><${associatedResource.href}><#sep>, </#items>;
</#list>
