<#escape x as x?xml>
<gmd:dataQualityInfo>
	<gmd:DQ_DataQuality>
		<gmd:scope>
			<gmd:DQ_Scope>
				<gmd:level>
					<gmd:MD_ScopeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_ScopeCode" codeListValue="${recordType}">${recordType}</gmd:MD_ScopeCode>
				</gmd:level>
				<gmd:levelDescription>
					<gmd:MD_ScopeDescription>
						<gmd:other><gco:CharacterString>${recordType}</gco:CharacterString></gmd:other>
					</gmd:MD_ScopeDescription>
				</gmd:levelDescription>
			</gmd:DQ_Scope>
		</gmd:scope>
		<#if conformanceResults?has_content>
		<#list conformanceResults as conformanceResult>
		<gmd:report>
		<gmd:DQ_DomainConsistency>
			<gmd:result>
				<gmd:DQ_ConformanceResult>
					<gmd:specification>
						<gmd:CI_Citation>
							<gmd:title>
								<gco:CharacterString>${conformanceResult.title}</gco:CharacterString>
							</gmd:title>
              <#if conformanceResult.date??>
							<gmd:date>
								<gmd:CI_Date>
									<gmd:date>
										<gco:Date>${conformanceResult.date}</gco:Date>
									</gmd:date>
									<gmd:dateType>
										<gmd:CI_DateTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode" codeListValue="${conformanceResult.dateType}">${conformanceResult.dateType}</gmd:CI_DateTypeCode>
									</gmd:dateType>
								</gmd:CI_Date>
							</gmd:date>
              </#if>
						</gmd:CI_Citation>
					</gmd:specification>
					<gmd:explanation>
						<gco:CharacterString>${conformanceResult.explanation}</gco:CharacterString>
					</gmd:explanation>
					<gmd:pass>
						<gco:Boolean>${conformanceResult.pass?c}</gco:Boolean>
					</gmd:pass>
				</gmd:DQ_ConformanceResult>
			</gmd:result>
		</gmd:DQ_DomainConsistency>
		</gmd:report>
		</#list>
		</#if>
		<#if lineage?has_content>
		<gmd:lineage>
			<gmd:LI_Lineage>
				<gmd:statement><gco:CharacterString>${lineage}</gco:CharacterString></gmd:statement>
			</gmd:LI_Lineage>
		</gmd:lineage>
		</#if>
	</gmd:DQ_DataQuality>
</gmd:dataQualityInfo>
</#escape>
