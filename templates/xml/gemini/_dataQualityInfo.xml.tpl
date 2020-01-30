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
		<#--
		Conformity (gmd:report) is mandatory for GEMINI 2.3 
		But we don't yet evaluate (or record) conformity so this is
		a boilerplate statement specifying that conformity has not been evaluated
		-->
		<gmd:report>
			<gmd:DQ_DomainConsistency>
				<gmd:result>
					<gmd:DQ_ConformanceResult>
						<gmd:specification>
							<gmd:CI_Citation>
								<gmd:title>
									<gmx:Anchor xlink:href="http://data.europa.eu/eli/reg/2010/1089">Commission Regulation (EU) No 1089/2010 of 23 November 2010 implementing Directive 2007/2/EC of the European Parliament and of the Council as regards interoperability of spatial data sets and services</gmx:Anchor>
								</gmd:title>
								<gmd:date>
									<gmd:CI_Date>
										<gmd:date>
											<gco:Date>2010-12-08</gco:Date>
										</gmd:date>
										<gmd:dateType>
											<gmd:CI_DateTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#CI_DateTypeCode" codeListValue="publication"/>
										</gmd:dateType>
									</gmd:CI_Date>
								</gmd:date>
							</gmd:CI_Citation>
						</gmd:specification>
						<gmd:explanation gco:nilReason="inapplicable"/>
						<gmd:pass gco:nilReason="unknown"/>
					</gmd:DQ_ConformanceResult>
				</gmd:result>
			</gmd:DQ_DomainConsistency>
		</gmd:report>
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
