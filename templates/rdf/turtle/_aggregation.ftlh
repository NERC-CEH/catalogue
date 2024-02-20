a dcmitype:Collection ;
<#-- TODO: replace with call to jena -->
<#if associatedResources?has_content>
dct:hasPart
<#list associatedResources as associatedResource>
    <${associatedResource.href}><#sep>,
</#list>
;
</#if>
