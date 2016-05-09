<#escape x as x?xml>
<#list useConstraints as useConstraint>
  <gmd:resourceConstraints>
    <gmd:MD_LegalConstraints>
      <gmd:useConstraints>
        <gmd:MD_RestrictionCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_RestrictionCode" codeListValue="${useConstraint.value}">${useConstraint.value}</gmd:MD_RestrictionCode>
      </gmd:useConstraints>
      <gmd:otherConstraints>
        <gmx:Anchor xlink:title="${useConstraint.label}" xlink:href="${useConstraint.uri}">${useConstraint.label}</gmx:Anchor>
      </gmd:otherConstraints>
    </gmd:MD_LegalConstraints>
  </gmd:resourceConstraints>
</#list>
<#if citation?has_content>
  <gmd:resourceConstraints>
    <gmd:MD_LegalConstraints>
      <gmd:useLimitation>
        <gco:CharacterString>If you reuse this data, you must cite: ${citation.authors?join(', ')} (${citation.year}). ${citation.title}. ${citation.publisher} ${citation.doi}</gco:CharacterString>
      </gmd:useLimitation>
    </gmd:MD_LegalConstraints>
  </gmd:resourceConstraints>
</#if>
<#list accessConstraints as accessConstraint>
  <gmd:resourceConstraints>
    <gmd:MD_LegalConstraints>
      <gmd:accessConstraints>
        <gmd:MD_RestrictionCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_RestrictionCode" codeListValue="otherRestrictions">otherRestrictions</gmd:MD_RestrictionCode>
      </gmd:accessConstraints>
      <gmd:otherConstraints>
        <gmx:Anchor xlink:title="${accessConstraint.label}" xlink:href="${accessConstraint.uri}">${accessConstraint.label}</gmx:Anchor>
      </gmd:otherConstraints>
    </gmd:MD_LegalConstraints>
  </gmd:resourceConstraints>
</#list>
</#escape>
