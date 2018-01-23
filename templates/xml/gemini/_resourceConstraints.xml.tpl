<#escape x as x?xml>
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
<#if resourceStatus?? && resourceStatus == "Embargoed" >
	<gmd:resourceConstraints>
		<gmd:MD_LegalConstraints>
			<gmd:accessConstraints>
				<gmd:MD_RestrictionCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_RestrictionCode" codeListValue="otherRestrictions">otherRestrictions</gmd:MD_RestrictionCode>
			</gmd:accessConstraints>
			<gmd:otherConstraints>
					<gco:CharacterString>This resource is currently under embargo</gco:CharacterString>
			</gmd:otherConstraints>
		</gmd:MD_LegalConstraints>
	</gmd:resourceConstraints>
<#elseif resourceStatus?? && resourceStatus == "Superseded" || resourceStatus = "Withdrawn" >
	<gmd:resourceConstraints>
		<gmd:MD_LegalConstraints>
			<gmd:accessConstraints>
				<gmd:MD_RestrictionCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_RestrictionCode" codeListValue="otherRestrictions">otherRestrictions</gmd:MD_RestrictionCode>
			</gmd:accessConstraints>
			<gmd:otherConstraints>
					<gco:CharacterString>This dataset has been withdrawn. If you need access, please email eidc@ceh.ac.uk</gco:CharacterString>
			</gmd:otherConstraints>
		</gmd:MD_LegalConstraints>
	</gmd:resourceConstraints>
<#else>
	<#if accessConstraints?has_content>
		<#list accessConstraints as accessConstraint>
			<gmd:resourceConstraints>
				<gmd:MD_LegalConstraints>
					<gmd:accessConstraints>
						<gmd:MD_RestrictionCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_RestrictionCode" codeListValue="otherRestrictions">otherRestrictions</gmd:MD_RestrictionCode>
					</gmd:accessConstraints>
					<gmd:otherConstraints>
							<gco:CharacterString>${accessConstraint.value}</gco:CharacterString>
					</gmd:otherConstraints>
				</gmd:MD_LegalConstraints>
			</gmd:resourceConstraints>
		</#list>
	<#else>
		<gmd:resourceConstraints>
				<gmd:MD_LegalConstraints>
					<gmd:accessConstraints>
						<gmd:MD_RestrictionCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_RestrictionCode" codeListValue="otherRestrictions">otherRestrictions</gmd:MD_RestrictionCode>
					</gmd:accessConstraints>
			<gmd:otherConstraints>
				<gco:CharacterString>no limitations</gco:CharacterString>
			</gmd:otherConstraints>
				</gmd:MD_LegalConstraints>
			</gmd:resourceConstraints>
	</#if>
</#if>
</#escape>
