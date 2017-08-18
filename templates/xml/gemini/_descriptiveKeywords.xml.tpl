<#escape x as x?xml>
<#if descriptiveKeywords??>
<#list descriptiveKeywords as descriptiveKeyword>
<gmd:descriptiveKeywords>
  <gmd:MD_Keywords>
    <#list descriptiveKeyword.keywords as keyword>
    <gmd:keyword>
      <gco:CharacterString>${keyword.value}</gco:CharacterString>
    </gmd:keyword>
    </#list>
    <#if (descriptiveKeyword.type??) && (
        (descriptiveKeyword.type == 'discipline') ||
        (descriptiveKeyword.type == 'place') ||
        (descriptiveKeyword.type == 'stratum') ||
        (descriptiveKeyword.type == 'temporal') ||
        (descriptiveKeyword.type == 'theme')
        )
    >
      <gmd:type>
        <gmd:MD_KeywordTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_KeywordTypeCode" codeListValue="${descriptiveKeyword.type}">${descriptiveKeyword.type}</gmd:MD_KeywordTypeCode>
      </gmd:type>
    </#if>
  </gmd:MD_Keywords>
</gmd:descriptiveKeywords>
</#list>
</#if>
</#escape>
