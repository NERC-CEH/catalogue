<#escape x as x?xml>
<#if useConstraints??>
  <#list useConstraints as useConstraint>
    <gmd:resourceConstraints>
      <gmd:MD_Constraints>
        <gmd:useLimitation>
          <#if useConstraint.uri?has_content>
            <gmx:Anchor xlink:title="${useConstraint.value}" xlink:href="${useConstraint.uri}">${useConstraint.value}</gmx:Anchor>
          <#else>
            <gco:CharacterString>${useConstraint.value}</gco:CharacterString>
          </#if>
        </gmd:useLimitation>
      </gmd:MD_Constraints>
    </gmd:resourceConstraints>
  </#list>
</#if>
<#if citation??>
  <gmd:resourceConstraints>
    <gmd:MD_Constraints>
      <gmd:useLimitation>
        <gco:CharacterString>If you reuse this data, you must cite: ${citation.authors?join(', ')} (${citation.year}). ${citation.title}. ${citation.publisher} ${citation.doi}</gco:CharacterString>
      </gmd:useLimitation>
    </gmd:MD_Constraints>
  </gmd:resourceConstraints>
</#if>
<#if accessConstraints??>
  <#list accessConstraints as accessConstraint>
    <gmd:resourceConstraints>
      <gmd:MD_LegalConstraints>
        <gmd:accessConstraints>
          <gmd:MD_RestrictionCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_RestrictionCode" codeListValue="otherRestrictions">otherRestrictions</gmd:MD_RestrictionCode>
        </gmd:accessConstraints>
        <gmd:accessConstraints>
          <gmd:MD_RestrictionCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_RestrictionCode" codeListValue="${accessConstraint.code}">${accessConstraint.code}</gmd:MD_RestrictionCode>
        </gmd:accessConstraints>
        <gmd:otherConstraints>
          <#if accessConstraint.uri?has_content>
            <gmx:Anchor xlink:title="${accessConstraint.value}" xlink:href="${accessConstraint.uri}">${accessConstraint.value}</gmx:Anchor>
          <#else>
            <gco:CharacterString>${accessConstraint.value}</gco:CharacterString>
          </#if>
        </gmd:otherConstraints>
      </gmd:MD_LegalConstraints>
    </gmd:resourceConstraints>
  </#list>
</#if>
</#escape>
