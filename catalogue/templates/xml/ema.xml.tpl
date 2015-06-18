<#import "efBase.ftl" as base/>
<#compress>
<?xml version="1.0" encoding="UTF-8"?>
<ef:EnvironmentalMonitoringActivity gml:id="ID_0" xsi:schemaLocation="http://inspire.ec.europa.eu/schemas/ef/3.0rc3 http://inspire.ec.europa.eu/draft-schemas/ef/3.0rc3/EnvironmentalMonitoringFacilities.xsd" xmlns:base="http://inspire.ec.europa.eu/schemas/base/3.3rc3/" xmlns:base2="http://inspire.ec.europa.eu/schemas/base2/1.0rc3" xmlns:ef="http://inspire.ec.europa.eu/schemas/ef/3.0rc3" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink">
    <@base.stdObjProps/>
    <#-- TODO: why do we have multiple bounding box? EF doesn't -->
    <@base.boundedBy boundingBoxes[0]!/>
    <@base.orNilReason "ef:activityTime" lifespan!>
        <ef:activityTime>
            <@base.timePeriod lifespan "at_0"/>
        </ef:activityTime>
    </@base.orNilReason>
    <@base.orNilReason "ef:activityConditions" description!>
        <ef:activityConditions>${description}</ef:activityConditions>
    </@base.orNilReason>
    <#if boundingBoxes[0]?has_content>
        <@base.boundedBy boundingBoxes[0]/>
    </#if>
    <@base.responsibleParty metadata.author/>
	<@base.inspireId/>
    <@base.orNilReason "ef:onlineResource" onlineResources!>
        <#list onlineResources as x>
            <@base.onlineResource x/>
        </#list>
    </@base.orNilReason>
    <@base.orNilReason "ef:setUpFor" setupFor!>
        <#list setupFor as x>
            <@base.link "setUpFor" x/>
        </#list>
    </@base.orNilReason>
    <#list uses as x>
        <@base.link "uses" x/>
    </#list>
</ef:EnvironmentalMonitoringActivity>
</#compress>
