<#escape x as x?xml>
<#if resourceStatus?? && resourceStatus == "embargoed" >
	<gmd:resourceConstraints>
		<gmd:MD_Constraints>
			<gmd:useLimitation>
				<gco:CharacterString>no restrictions apply</gco:CharacterString>
			</gmd:useLimitation>
		</gmd:MD_Constraints>
	</gmd:resourceConstraints>
<#else>
	<#if useConstraints?? || citation??>
	<gmd:resourceConstraints>
		<gmd:MD_Constraints>
		<#if useConstraints??>
			<#list useConstraints as useConstraint>
				<gmd:useLimitation>
					<#if useConstraint.uri?has_content>
					<gmx:Anchor xlink:title="${useConstraint.value}" xlink:href="${useConstraint.uri}">${useConstraint.value}</gmx:Anchor>
					<#else>
					<gco:CharacterString>${useConstraint.value}</gco:CharacterString>
					</#if>
				</gmd:useLimitation>
			</#list>
		</#if>
		<#if citation??>
				<gmd:useLimitation>
				<gco:CharacterString>If you reuse this data, you should cite: ${citation.authors?join(', ')} (${citation.year}). ${citation.title}. ${citation.publisher} ${citation.url}</gco:CharacterString>
				</gmd:useLimitation>
		</#if>
			</gmd:MD_Constraints>
	</gmd:resourceConstraints>
	</#if>
</#if>

<#if accessLimitation??>
	<gmd:resourceConstraints>
		<gmd:MD_LegalConstraints>
			<gmd:accessConstraints>
				<gmd:MD_RestrictionCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_RestrictionCode" codeListValue="otherRestrictions">otherRestrictions</gmd:MD_RestrictionCode>
			</gmd:accessConstraints>
			<gmd:otherConstraints>
				<#if accessLimitation.uri?has_content >
					<gmx:Anchor xlink:href="${accessLimitation.uri}">${accessLimitation.value}</gmx:Anchor>
				<#else>
					<gco:CharacterString>${accessLimitation.value}</gco:CharacterString>
				</#if>
			</gmd:otherConstraints>
		</gmd:MD_LegalConstraints>
	</gmd:resourceConstraints>
</#if>

</#escape>