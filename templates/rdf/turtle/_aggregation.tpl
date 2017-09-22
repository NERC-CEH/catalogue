a dcmitype:Collection ;

<#if associatedResources?has_content>
dct:hasPart
<#list associatedResources as associatedResource>
    <${associatedResource.href}><#sep>,
</#list>
;
</#if>
