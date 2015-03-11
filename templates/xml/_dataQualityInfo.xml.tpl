<gmd:dataQualityInfo>
	<gmd:DQ_DataQuality>
		<gmd:scope>
			<gmd:DQ_Scope>
				<gmd:level>
					<MD_ScopeCode xmlns="http://www.isotc211.org/2005/gmd" codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_ScopeCode" codeListValue="${resourceType}"/>
				</gmd:level>
				<gmd:levelDescription>
					<gmd:MD_ScopeDescription>
						<gmd:other><gco:CharacterString>${resourceType?xml}</gco:CharacterString></gmd:other>
					</gmd:MD_ScopeDescription>
				</gmd:levelDescription>
			</gmd:DQ_Scope>
		</gmd:scope>
		<#if lineage?has_content>
		<gmd:lineage>
			<gmd:LI_Lineage>
				<gmd:statement><gco:CharacterString>${lineage?xml}</gco:CharacterString></gmd:statement>
			</gmd:LI_Lineage>
		</gmd:lineage>
		</#if>
	</gmd:DQ_DataQuality>
</gmd:dataQualityInfo>