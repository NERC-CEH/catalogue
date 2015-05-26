<gmd:identificationInfo>
	<#if type = 'service'>
	<srv:SV_ServiceIdentification>
	<#else>
	<gmd:MD_DataIdentification>
	</#if>
		<gmd:citation>
			<gmd:CI_Citation>
				<gmd:title>
					<gco:CharacterString>${title?xml}</gco:CharacterString>
				</gmd:title>
				<#if datasetReferenceDate?has_content>
				<#if datasetReferenceDate.publicationDate?has_content>
				<gmd:date>
					<gmd:CI_Date>
						<gmd:date>
							<gco:Date>${datasetReferenceDate.publicationDate?xml}</gco:Date>
						</gmd:date>
						<gmd:dateType>
							<CI_DateTypeCode xmlns="http://www.isotc211.org/2005/gmd" codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#CI_DateTypeCode" codeListValue="publication"/>
						</gmd:dateType>
					</gmd:CI_Date>
				</gmd:date>
				</#if>
				<#if datasetReferenceDate.creationDate?has_content>
				<gmd:date>
					<gmd:CI_Date>
						<gmd:date>
							<gco:Date>${datasetReferenceDate.creationDate?xml}</gco:Date>
						</gmd:date>
						<gmd:dateType>
							<CI_DateTypeCode xmlns="http://www.isotc211.org/2005/gmd" codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#CI_DateTypeCode" codeListValue="creation"/>
						</gmd:dateType>
					</gmd:CI_Date>
				</gmd:date>
				</#if>
				<#if datasetReferenceDate.revisionDate?has_content>
				<gmd:date>
					<gmd:CI_Date>
						<gmd:date>
							<gco:Date>${datasetReferenceDate.revisionDate?xml}</gco:Date>
						</gmd:date>
						<gmd:dateType>
							<CI_DateTypeCode xmlns="http://www.isotc211.org/2005/gmd" codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#CI_DateTypeCode" codeListValue="revision"/>
						</gmd:dateType>
					</gmd:CI_Date>
				</gmd:date>
				</#if>
				</#if>
				<#if resourceIdentifiers?has_content>
				<#list resourceIdentifiers as uri>
				<gmd:identifier>
				<#if uri.code?starts_with("http://") || uri.code?starts_with("https://")>
				   <gmd:MD_Identifier>
				      <gmd:code>
					     <gco:CharacterString>${uri.code?xml}</gco:CharacterString>
					</gmd:code>
				   </gmd:MD_Identifier>
				<#else>
					<gmd:RS_Identifier>
						<gmd:code><gco:CharacterString>${uri.code?xml}</gco:CharacterString></gmd:code>
						<#if uri.codeSpace?has_content>
						<gmd:codeSpace><gco:CharacterString>${uri.codeSpace?xml}</gco:CharacterString></gmd:codeSpace>
						</#if>
						<#if uri.version?has_content>
						<gmd:version><gco:CharacterString>${uri.version?xml}</gco:CharacterString></gmd:version>
						</#if>
					</gmd:RS_Identifier>
				</#if>
				</gmd:identifier>
				</#list>
				</#if>
				<#if citation?has_content>
				<gmd:otherCitationDetails>
					<gco:CharacterString>${citation.authors?join(', ')?xml} (${citation.year?xml}). ${citation.title?xml}. ${citation.publisher?xml} ${citation.doi?xml}</gco:CharacterString>
				</gmd:otherCitationDetails>
				</#if>
			</gmd:CI_Citation>
		</gmd:citation>
		<gmd:abstract>
			<gco:CharacterString>${description!''?xml}</gco:CharacterString>
		</gmd:abstract>
    <#if resourceStatus??>
      <gmd:status>
        <MD_ProgressCode xmlns="http://www.isotc211.org/2005/gmd" codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_ProgressCode" codeListValue="${resourceStatus}"/>
      </gmd:status>
    </#if>
		<#if responsibleParties?has_content>
		<#list responsibleParties as responsibleParty>
		<gmd:pointOfContact>
			<gmd:CI_ResponsibleParty>
				<#if responsibleParty.individualName?has_content>
				<gmd:individualName><gco:CharacterString>${responsibleParty.individualName?xml}</gco:CharacterString></gmd:individualName>
				</#if>
				<#if responsibleParty.organisationName?has_content>
				<gmd:organisationName><gco:CharacterString>${responsibleParty.organisationName?xml}</gco:CharacterString></gmd:organisationName>
				</#if>
				<gmd:contactInfo>
					<gmd:CI_Contact>
						<gmd:address>
							<gmd:CI_Address>
								<#if responsibleParty.deliveryPoint?has_content>
								<gmd:deliveryPoint><gco:CharacterString>${responsibleParty.deliveryPoint?xml}</gco:CharacterString></gmd:deliveryPoint>
								</#if>
								<#if responsibleParty.city?has_content>
								<gmd:city><gco:CharacterString>${responsibleParty.city?xml}</gco:CharacterString></gmd:city>
								</#if>
								<#if responsibleParty.administrativeArea?has_content>
								<gmd:administrativeArea><gco:CharacterString>${responsibleParty.administrativeArea?xml}</gco:CharacterString></gmd:administrativeArea>
								</#if>
								<#if responsibleParty.postalCode?has_content>
								<gmd:postalCode><gco:CharacterString>${responsibleParty.postalCode?xml}</gco:CharacterString></gmd:postalCode>
								</#if>
								<#if responsibleParty.country?has_content>
								<gmd:country><gco:CharacterString>${responsibleParty.country?xml}</gco:CharacterString></gmd:country>
								</#if>
								<#if responsibleParty.email?has_content>
								<gmd:electronicMailAddress><gco:CharacterString>${responsibleParty.email?xml}</gco:CharacterString></gmd:electronicMailAddress>
								</#if>
							</gmd:CI_Address>
						</gmd:address>
					</gmd:CI_Contact>
				</gmd:contactInfo>
				<gmd:role>
					<gmd:CI_RoleCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#CI_RoleCode" codeListValue="${responsibleParty.role?xml}">${responsibleParty.role?xml}</gmd:CI_RoleCode>
				</gmd:role>
			</gmd:CI_ResponsibleParty>
		</gmd:pointOfContact>
		</#list>
		</#if>
    <#if resourceMaintenance??>
      <#list resourceMaintenance as rm>
        <gmd:resourceMaintenance>
          <gmd:MD_MaintenanceInformation>
            <#if rm.frequencyOfUpdate?has_content>
            <gmd:maintenanceAndUpdateFrequency>
              <gmd:MD_MaintenanceFrequencyCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_MaintenanceFrequencyCode" codeListValue="${rm.frequencyOfUpdate?xml}"/>
            </gmd:maintenanceAndUpdateFrequency>
            </#if>
            <#if rm.note?has_content>
            <gmd:maintenanceNote>
              <gco:CharacterString>${rm.note?xml}</gco:CharacterString>
            </gmd:maintenanceNote>
            </#if>
          </gmd:MD_MaintenanceInformation>
        </gmd:resourceMaintenance>
      </#list>
    </#if>
		<#include "_descriptiveKeywords.xml.tpl">
		<#include "_resourceConstraints.xml.tpl">
		<#if spatialRepresentationTypes?has_content>
		<#list spatialRepresentationTypes as spatialRepresentationType>
		<gmd:spatialRepresentationType>
			<gmd:MD_SpatialRepresentationTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_SpatialRepresentationTypeCode" codeListValue="${spatialRepresentationType?xml}"/>
		</gmd:spatialRepresentationType>
		</#list>
		</#if>
		<#if spatialResolutions?has_content>
		<#list spatialResolutions as spatialResolution>
			<#if spatialResolution.equivalentScale?has_content>
			  <gmd:spatialResolution>
				<gmd:MD_Resolution>
				   <gmd:equivalentScale>
					  <gmd:MD_RepresentativeFraction>
						 <gmd:denominator>
							<gco:Integer>${spatialResolution.equivalentScale}</gco:Integer>
						 </gmd:denominator>
					  </gmd:MD_RepresentativeFraction>
				   </gmd:equivalentScale>
				</gmd:MD_Resolution>
			 </gmd:spatialResolution>
			</#if>
			<#if spatialResolution.distance?has_content>
			 <gmd:spatialResolution>
				<gmd:MD_Resolution>
				   <gmd:distance>
					  <gco:Distance uom="${spatialResolution.uom?xml}">${spatialResolution.distance}</gco:Distance>
				   </gmd:distance>
				</gmd:MD_Resolution>
			 </gmd:spatialResolution>
			</#if>
		</#list>
		</#if>
		<#if type != 'service'>
		<gmd:language>
			<LanguageCode xmlns="http://www.isotc211.org/2005/gmd" codeList="http://www.loc.gov/standards/iso639-2/php/code_list.php" codeListValue="eng"/>
		</gmd:language>
		<gmd:characterSet>
			<MD_CharacterSetCode xmlns="http://www.isotc211.org/2005/gmd" codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_CharacterSetCode" codeListValue="utf8"/>
		</gmd:characterSet>	
		</#if>
		<#if topicCategories??>
		<#list topicCategories as topicCategory>
      <#if topicCategory.value?has_content>
        <gmd:topicCategory>
          <gmd:MD_TopicCategoryCode>${topicCategory.value?xml}</gmd:MD_TopicCategoryCode>
        </gmd:topicCategory>
      </#if>
		</#list>
		</#if>
		
	<#if type = 'service'>
	<#assign ns = 'srv'>
	<#include "_service.xml.tpl">
	</srv:SV_ServiceIdentification>
	<#else>
	<#assign ns = 'gmd'>
	<#include "_extent.xml.tpl">
	</gmd:MD_DataIdentification>
	</#if>

</gmd:identificationInfo>
