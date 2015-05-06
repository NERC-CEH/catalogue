<gmd:resourceConstraints>
	<gmd:MD_LegalConstraints>
	<#if useLimitations?has_content>
	<#list useLimitations as useLimitation>
		<gmd:useLimitation><gco:CharacterString>${useLimitation?xml}</gco:CharacterString></gmd:useLimitation>
	</#list>
	</#if>
	<#if citation?has_content>
		<gmd:useLimitation><gco:CharacterString>If you reuse this data, you must cite: ${citation.authors?join(', ')?xml} (${citation.year?xml}). ${citation.title?xml}. ${citation.publisher?xml} ${citation.doi?xml}</gco:CharacterString></gmd:useLimitation>
	</#if>
		<gmd:accessConstraints><#--'otherRestrictions' must always be present so hard-coding into the template -->
			<gmd:MD_RestrictionCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_RestrictionCode" codeListValue="otherRestrictions"/>
		</gmd:accessConstraints>
	<#if accessConstraints?has_content>
	<#list accessConstraints as accessConstraint>
		<#if accessConstraint != 'otherRestrictions' >
		<gmd:accessConstraints>
			<gmd:MD_RestrictionCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_RestrictionCode" codeListValue="${accessConstraint?xml}"/>
		</gmd:accessConstraints>
		</#if>
	</#list>
	</#if>
	<#if otherConstraints?has_content>
	<#list otherConstraints as otherConstraint>
		<gmd:otherConstraints><gco:CharacterString>${otherConstraint?xml}</gco:CharacterString></gmd:otherConstraints>
	</#list>
		<#--<gmd:otherConstraints>
			<gmx:Anchor xlink:href="http://eidchub.ceh.ac.uk/administration-folder/tools/ceh-standard-licence-texts/open-government-licence-lidar-tellus/plain">This resource is available under the Open Government Licence (OGL)</gmx:Anchor>
		</gmd:otherConstraints>-->
	</#if>
	</gmd:MD_LegalConstraints>
</gmd:resourceConstraints>