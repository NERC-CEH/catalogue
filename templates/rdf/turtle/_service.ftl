a dcmitype:Service ;
<#-- A service record with a WMS link is itself the DataService; every link is an endpoint -->
<#list wmsUris>
a dcat:DataService ;
dcat:endpointURL <#items as wmsUri><${wmsUri}><#sep>, </#sep></#items> ;
<#-- The Jena index records a service's coupledResources as dcterms:requires. Same guards as the
     requires block in _body.ftl, plus the target must be a dataset: servesDataset ranges over
     dcat:Dataset. associationType carries the target's dcterms:type. -->
<#list jena.relationships(uri, "http://purl.org/dc/terms/requires")?filter(item -> item.availability != "Deleted" && item.publicationStatus == "published" && item.associationType == "dataset")>
dcat:servesDataset <#items as item><${item.href}><#sep>, </#sep></#items> ;
</#list>
</#list>
<#include "_rights.ftl">
