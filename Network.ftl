<#import "base.ftl" as base/>
<#compress>
<?xml version="1.0" encoding="UTF-8"?>
<ef:EnvironmentalMonitoringNetwork gml:id="ID_0" xsi:schemaLocation="http://inspire.ec.europa.eu/schemas/ef/3.0rc3 http://inspire.ec.europa.eu/draft-schemas/ef/3.0rc3/EnvironmentalMonitoringFacilities.xsd" xmlns:base="http://inspire.ec.europa.eu/schemas/base/3.3rc3/" xmlns:base2="http://inspire.ec.europa.eu/schemas/base2/1.0rc3" xmlns:ef="http://inspire.ec.europa.eu/schemas/ef/3.0rc3" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink">
    <@base.stdObjProps/>
    <#-- TODO: why do we have multiple bounding box? EF doesn't -->
    <@base.boundedBy boundingBoxes[0]!/>
	<@base.inspireId/>
    <@base.orNilReason "ef:name" name!>
        <ef:name>${name}</ef:name>
    </@base.orNilReason>
    <@base.orNilReason "ef:additionalDescription" description!>
        <ef:additionalDescription>${description}</ef:additionalDescription>
    </@base.orNilReason>
    <#-- TODO: what do we put in mediaMonitored, should be air, sea, water, etc -->
    <ef:mediaMonitored nilReason="missing"/>
    <@base.orNilReason "ef:legalBackground" legislation!>
        <#list legislation as x>
            <@base.link "legalBackground" x/>
        </#list>
    </@base.orNilReason>
    <@base.responsibleParty metadata.author/>
    <#list responsibleParties as x>
        <@base.responsibleParty x/>
    </#list>
    <ef:geometry nilReason="missing"/>
    <@base.orNilReason "ef:onlineResource" onlineResources!>
        <#list onlineResources as x>
            <@base.onlineResource x/>
        </#list>
    </@base.orNilReason>
    <ef:purpose nilReason="missing"/>
    <@base.orNilReason "ef:observingCapability" observingCapabilities! false>
        <#list observingCapabilities as x>
            <@base.observingCapability x x_index/>
        </#list>
    </@base.orNilReason>
    <@base.orNilReason "ef:broader" broaderThan!>
        <#-- first broader than in list, why list? -->
        <@base.link "broader" broaderThan[0]/>
    </@base.orNilReason>
    <@base.orNilReason "ef:narrower" narrowerThan!>
        <#list narrowerThan as x>
            <@base.link "narrower" x/>
        </#list>
    </@base.orNilReason>
    <@base.orNilReason "ef:supersedes" supersedes!>
        <#list supersedes as x>
            <@base.link "supersedes" x/>
        </#list>
    </@base.orNilReason>
    <@base.orNilReason "ef:supersededBy" supersededBy!>
        <#list supersededBy as x>
            <@base.link "supersededBy" x/>
        </#list>
    </@base.orNilReason>
    <#-- Want to add more elements and then cannot have nil reportingDate <ef:reportedTo nilReason="missing"/>-->
    <@base.orNilReason "ef:hasObservation" observations!>
        <#list observations as x>
            <@base.link "hasObservations" x/>
        </#list>
    </@base.orNilReason>
    <@base.orNilReason "ef:involvedIn" involvedIn!>
        <#list involvedIn as x>
            <@base.link "involvedIn" x/>
        </#list>
    </@base.orNilReason>
    <ef:organisationLevel nilReason="missing"/>
    <@base.orNilReason "ef:contains" contains!>
        <#list contains as x>
            <@base.link "contains" x/>
        </#list>
    </@base.orNilReason>
</ef:EnvironmentalMonitoringNetwork>
</#compress>