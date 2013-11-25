<#compress>
<?xml version="1.0" encoding="UTF-8"?>
<ef:EnvironmentalMonitoringProgramme gml:id="ID_0" xsi:schemaLocation="http://inspire.ec.europa.eu/schemas/ef/3.0rc3 http://inspire.ec.europa.eu/draft-schemas/ef/3.0rc3/EnvironmentalMonitoringFacilities.xsd" xmlns:base="http://inspire.ec.europa.eu/schemas/base/3.3rc3/" xmlns:base2="http://inspire.ec.europa.eu/schemas/base2/1.0rc3" xmlns:ef="http://inspire.ec.europa.eu/schemas/ef/3.0rc3" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xlink="http://www.w3.org/1999/xlink">
	<#if description?has_content>
        <gml:description>${description}</gml:description>
    </#if>
    <#-- why do we have multiple bounding box? EF doesn't -->
    <#if boundingBoxes[0]?has_content>
        <#assign box = boundingBoxes[0]/>
        <gml:boundedBy>
            <gml:Envelope>
                <gml:lowerCorner>23.45 -3.56</gml:lowerCorner>
                <gml:upperCorner>56.45 1.03</gml:upperCorner>
            </gml:Envelope>
        </gml:boundedBy>
    </#if>
	<ef:inspireId>
		<base:Identifier>
			<base:localId>${getMetadata().fileIdentifier}</base:localId>
			<base:namespace>${ukeofNamespace}</base:namespace>
		</base:Identifier>
	</ef:inspireId>
	<ef:name>${name}</ef:name>
	<#list alternativeNames as alternativeName>
		<ef:name>${alternativeName}</ef:name>
	</#list>
    <#-- what do we put in mediaMonitored, should be air, sea, water, etc -->
	<ef:mediaMonitored nilReason="missing"/>
    <#list legislation as legal>
        <ef:legalBackground xlink:title="${legal.title}" xlink:href="${legal.href}"/>
    </#list>
	<@responsibleParty metadata.author/>
	<#list responsibleParties as rp>
		<@responsibleParty rp/>
	</#list> 
	<#-- first broader than in list, why list? -->
	<#--<ef:broader xlink:title="${broaderThan.title}" xlink:href="${broaderThan.href}"/>-->
	<#list narrowerThan as narrower>
		<#if narrower.title?has_content>
			<ef:narrower xlink:title="${narrower.title}" xlink:href="${narrower.href}"/>
		<#else>
			<ef:narrower xlink:href="${narrower.href}"/>
		</#if>
	</#list>
</ef:EnvironmentalMonitoringProgramme>
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