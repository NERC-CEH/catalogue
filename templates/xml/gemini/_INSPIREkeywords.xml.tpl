<#escape x as x?xml>
<#if descriptiveKeywords??>
<#list descriptiveKeywords as descriptiveKeyword>
<#if (descriptiveKeyword.type??) && (descriptiveKeyword.type == 'INSPIRE Theme')>
<gmd:descriptiveKeywords>
  <gmd:MD_Keywords>
    <#list descriptiveKeyword.keywords as keyword>
    <gmd:keyword>
      <gco:CharacterString>${keyword.value}</gco:CharacterString>
    </gmd:keyword>
    </#list>
      <gmd:type>
        <gmd:MD_KeywordTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_KeywordTypeCode" codeListValue="theme">theme</gmd:MD_KeywordTypeCode>
      </gmd:type>
    <gmd:thesaurusName>
      <gmd:CI_Citation>
        <gmd:title>
          <gco:CharacterString>GEMET - INSPIRE themes, version 1.0</gco:CharacterString>
        </gmd:title>
        <gmd:date>
          <gmd:CI_Date>
            <gmd:date>
              <gco:Date>2008-06-01</gco:Date>
            </gmd:date>
            <gmd:dateType>
              <gmd:CI_DateTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#CI_DateTypeCode" codeListValue="publication">publication</gmd:CI_DateTypeCode>
            </gmd:dateType>
          </gmd:CI_Date>
        </gmd:date>
      </gmd:CI_Citation>
    </gmd:thesaurusName>
  </gmd:MD_Keywords>
</gmd:descriptiveKeywords>
</#if>
</#list>
</#if>
</#escape>
