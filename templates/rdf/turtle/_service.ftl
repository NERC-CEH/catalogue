a dcmitype:Service ;
<#-- A service record with a WMS link is itself the DataService; every link is an endpoint -->
<#list wmsUris>
a dcat:DataService ;
dcat:endpointURL <#items as wmsUri><${wmsUri}><#sep>, </#sep></#items> ;
</#list>
<#include "_rights.ftl">
