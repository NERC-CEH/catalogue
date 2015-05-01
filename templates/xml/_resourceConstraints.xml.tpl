<gmd:resourceConstraints>
	<gmd:MD_LegalConstraints>
	<#if useLimitations?has_content>
	<#list useLimitations as useLimitation>
    <#if useLimitation.uri?has_content>
      <gmd:useLimitation><gmx:Anchor xlink:title="${useLimitation.value?xml}" xlink:href="${useLimitation.uri?xml}"/></gmd:useLimitation>
    <#else>
      <gmd:useLimitation><gco:CharacterString>${useLimitation.value?xml}</gco:CharacterString></gmd:useLimitation>
    </#if>
	</#list>
	</#if>
	<#if citation?has_content>
		<gmd:useLimitation><gco:CharacterString>If you reuse this data, you must cite: ${citation.authors?join(', ')?xml} (${citation.year?xml}). ${citation.title?xml}. ${citation.publisher?xml} ${citation.doi?xml}</gco:CharacterString></gmd:useLimitation>
	</#if>
		<gmd:accessConstraints><#--'otherRestrictions' must always be present so hard-coding into the template -->
			<MD_RestrictionCode xmlns="http://www.isotc211.org/2005/gmd" codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_RestrictionCode" codeListValue="otherRestrictions"/>
		</gmd:accessConstraints>
	<#if accessConstraints?has_content>
	<#list accessConstraints as accessConstraint>
		<#if accessConstraint != 'otherRestrictions' >
		<gmd:accessConstraints>
			<MD_RestrictionCode xmlns="http://www.isotc211.org/2005/gmd" codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_RestrictionCode" codeListValue="${accessConstraint?xml}"/>
		</gmd:accessConstraints>
		</#if>
	</#list>
	</#if>
	<#if otherConstraints?has_content>
	<#list otherConstraints as otherConstraint>
    <#if otherConstraint.uri?has_content>
      <gmd:otherConstraints><gmx:Anchor xlink:title="${otherConstraint.value?xml}" xlink:href="${otherConstraint.uri?xml}"/></gmd:otherConstraints>
    <#else>
      <gmd:otherConstraints><gco:CharacterString>${otherConstraint.value?xml}</gco:CharacterString></gmd:otherConstraints>
    </#if>
	</#list>
	</#if>
	</gmd:MD_LegalConstraints>
</gmd:resourceConstraints>