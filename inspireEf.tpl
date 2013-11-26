<#assign type>${getClass().getSimpleName()}</#assign>
<#compress>
<?xml version="1.0" encoding="UTF-8"?>
<ef:EnvironmentalMonitoring${type} gml:id="ID_0" xsi:schemaLocation="http://inspire.ec.europa.eu/schemas/ef/3.0rc3 http://inspire.ec.europa.eu/draft-schemas/ef/3.0rc3/EnvironmentalMonitoringFacilities.xsd" xmlns:base="http://inspire.ec.europa.eu/schemas/base/3.3rc3/" xmlns:base2="http://inspire.ec.europa.eu/schemas/base2/1.0rc3" xmlns:ef="http://inspire.ec.europa.eu/schemas/ef/3.0rc3" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink">
    <#if description?has_content>
        <gml:description>${description}</gml:description>
    </#if>
        <gml:name>${name}</gml:name>
	<#list alternativeNames as alternativeName>
		<gml:name>${alternativeName}</gml:name>
	</#list>
    <#-- TODO: why do we have multiple bounding box? EF doesn't -->
    <#if boundingBoxes[0]?has_content>
        <#assign box = boundingBoxes[0]/>
        <gml:boundedBy>
            <gml:Envelope>
                <gml:lowerCorner>${box.southBoundLatitude} ${box.westBoundLongitude}</gml:lowerCorner>
                <gml:upperCorner>${box.northBoundLatitude} ${box.eastBoundLongitude}</gml:upperCorner>
            </gml:Envelope>
        </gml:boundedBy>
    </#if>
    <#if lifespan??>
        <ef:activityTime>
            <gml:TimePeriod gml:id="validTimeRange">
                <gml:beginPosition>${lifespan.start}</gml:beginPosition>
                <#if lifespan.end??>
                    <gml:endPosition>${lifespan.end}</gml:endPosition>
                <#else>
                    <gml:endPosition indeterminatePosition="unknown"/>
                </#if>
            </gml:TimePeriod>
        </ef:activityTime>
    </#if>
    <#if type == "Activity">
        <#-- TODO: what are these? -->
        <ef:activityConditions nilReason="unknown"/>
        <ef:responsibleParty nilReason="unknown">
            <base2:RelatedParty/>
        </ef:responsibleParty>
    </#if>
	<ef:inspireId>
		<base:Identifier>
			<base:localId>${getMetadata().fileIdentifier}</base:localId>
			<base:namespace>${ukeofNamespace}</base:namespace>
		</base:Identifier>
	</ef:inspireId>
    <#if type == "Programme">
        <#-- TODO: what do we put in mediaMonitored, should be air, sea, water, etc -->
        <ef:mediaMonitored nilReason="missing"/>
    </#if>
    <#if legislation??>
        <#list legislation as l>
            <@link "legalBackground" l/>
        </#list>
    </#if>
	<@responsibleParty metadata.author/>
	<#list responsibleParties as rp>
		<@responsibleParty rp/>
	</#list>
    <#if broaderThan??>
        <#-- first broader than in list, why list? -->
        <#if broaderThan[0]?has_content>
            <@link "broader" broaderThan[0]/>
        </#if>
    </#if>
    <#if narrowerThan??>
        <#list narrowerThan as n>
            <@link "narrower" n/>
        </#list>
    </#if>
    <#if supersedes??>
        <#list supersedes as s>
            <@link "supersedes" s/>
        </#list>
    </#if>
    <#if supersededBy??>
        <#list supersededBy as s>
            <@link "supersededBy" s/>
        </#list>
    </#if>
    <#if triggers??>
        <#list triggers as t>
            <@link "triggers" t/>
        </#list>
    </#if>
</ef:EnvironmentalMonitoring${type}>
</#compress>

<#macro responsibleParty rp>
    <ef:responsibleParty>
		<base2:RelatedParty>
			<#if rp.individualName?has_content>
				<base2:individualName><gco:CharacterString>${rp.individualName}</gco:CharacterString></base2:individualName>
			</#if>
			<#if rp.organisationName?has_content>
				<base2:organisationName><gco:CharacterString>${rp.organisationName}</gco:CharacterString></base2:organisationName>
			</#if>
			<#if rp.position?has_content>
				<base2:positionName><gco:CharacterString>${rp.position}</gco:CharacterString></base2:positionName>
			</#if>
			<#if rp.email?has_content>
				<base2:contact>
					<base2:Contact>
						<base2:electronicMailAddress>${rp.email}</base2:electronicMailAddress>
					</base2:Contact>
				</base2:contact>
			</#if>
			<#if rp.role?has_content>
				<#-- link to role vocabulary -->
				<base2:role xlink:title="${rp.role.codeListValue}" xlink:href="${rp.role.codeList}"/>
			</#if>
		</base2:RelatedParty>
	</ef:responsibleParty>
</#macro>

<#macro link type link>
    <#if link.title?has_content>
        <ef:${type} xlink:title="${link.title}" xlink:href="${link.href}"/>
    <#else>
        <ef:${type} xlink:href="${link.href}"/>
    </#if>
</#macro>