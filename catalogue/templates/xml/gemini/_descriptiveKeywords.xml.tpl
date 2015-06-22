<#escape x as x?xml>
<#if descriptiveKeywords?has_content>
<#list descriptiveKeywords as descriptiveKeyword>
<gmd:descriptiveKeywords>
	<gmd:MD_Keywords>
		<#list descriptiveKeyword.keywords as keyword>
		<gmd:keyword>
			<gco:CharacterString>${keyword.value}</gco:CharacterString>
		</gmd:keyword>
		</#list>
		<#if descriptiveKeyword.type?has_content>
		<gmd:type>
			<gmd:MD_KeywordTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_KeywordTypeCode" codeListValue="${descriptiveKeyword.type}">${descriptiveKeyword.type}</gmd:MD_KeywordTypeCode>
		</gmd:type>
		</#if>
		<#if descriptiveKeyword.thesaurusName?has_content>
		<gmd:thesaurusName>
			<gmd:CI_Citation>
				<#if descriptiveKeyword.thesaurusName.title?has_content>
				<gmd:title>
					<gco:CharacterString>${descriptiveKeyword.thesaurusName.title}</gco:CharacterString>
				</gmd:title>
				</#if>
				<#if descriptiveKeyword.thesaurusName.date?has_content>
				<gmd:date>
					<gmd:CI_Date>
						<gmd:date>
							<gco:Date>${descriptiveKeyword.thesaurusName.date}</gco:Date>
						</gmd:date>
						<#if descriptiveKeyword.thesaurusName.dateType?has_content>
						<gmd:dateType>
							<gmd:CI_DateTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#CI_DateTypeCode" codeListValue="${descriptiveKeyword.thesaurusName.dateType}">${descriptiveKeyword.thesaurusName.dateType}</gmd:CI_DateTypeCode>
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
</#escape>
