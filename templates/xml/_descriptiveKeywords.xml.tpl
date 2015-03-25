<#if descriptiveKeywords?has_content>
<#list descriptiveKeywords as descriptiveKeyword>
<gmd:descriptiveKeywords>
	<gmd:MD_Keywords>
		<#list descriptiveKeyword.keywords as keyword>
		<gmd:keyword>
			<gco:CharacterString>${keyword.value?xml}</gco:CharacterString>
		</gmd:keyword>
		</#list>
		<#if descriptiveKeyword.type?has_content>
		<gmd:type>
			<MD_KeywordTypeCode xmlns="http://www.isotc211.org/2005/gmd" codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_KeywordTypeCode" codeListValue="${descriptiveKeyword.type?xml}"/>
		</gmd:type>
		</#if>
		<#if descriptiveKeyword.thesaurusName?has_content>
		<gmd:thesaurusName>
			<gmd:CI_Citation>
				<#if descriptiveKeyword.thesaurusName.title?has_content>
				<gmd:title>
					<gco:CharacterString>${descriptiveKeyword.thesaurusName.title?xml}</gco:CharacterString>
				</gmd:title>
				</#if>
				<#if descriptiveKeyword.thesaurusName.date?has_content>
				<gmd:date>
					<gmd:CI_Date>
						<gmd:date>
							<gco:Date>${descriptiveKeyword.thesaurusName.date?xml}</gco:Date>
						</gmd:date>
						<#if descriptiveKeyword.thesaurusName.dateType?has_content>
						<gmd:dateType>
							<CI_DateTypeCode xmlns="http://www.isotc211.org/2005/gmd" codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#CI_DateTypeCode" codeListValue="${descriptiveKeyword.thesaurusName.dateType?xml}"/>
						</gmd:dateType>
						</#if>
					</gmd:CI_Date>
				</gmd:date>
				</#if>
			</gmd:CI_Citation>
		</gmd:thesaurusName>
		</#if>
	</gmd:MD_Keywords>
</gmd:descriptiveKeywords>
</#list>
</#if>