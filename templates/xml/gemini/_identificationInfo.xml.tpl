<#escape x as x?xml>
<gmd:identificationInfo>
	<#if type = 'service'>
	<srv:SV_ServiceIdentification>
	<#else>
	<gmd:MD_DataIdentification>
	</#if>
		<gmd:citation>
			<gmd:CI_Citation>
				<gmd:title>
					<gco:CharacterString>${title}</gco:CharacterString>
				</gmd:title>
				<#if datasetReferenceDate?has_content>
				<#if datasetReferenceDate.publicationDate?has_content>
				<gmd:date>
					<gmd:CI_Date>
						<gmd:date>
							<gco:Date>${datasetReferenceDate.publicationDate}</gco:Date>
						</gmd:date>
						<gmd:dateType>
						<CI_DateTypeCode xmlns="http://www.isotc211.org/2005/gmd" codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#CI_DateTypeCode" codeListValue="publication">publication</CI_DateTypeCode>
						</gmd:dateType>
					</gmd:CI_Date>
				</gmd:date>
				</#if>
				<#if datasetReferenceDate.creationDate?has_content>
				<gmd:date>
					<gmd:CI_Date>
						<gmd:date>
							<gco:Date>${datasetReferenceDate.creationDate}</gco:Date>
						</gmd:date>
						<gmd:dateType>
							<CI_DateTypeCode xmlns="http://www.isotc211.org/2005/gmd" codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#CI_DateTypeCode" codeListValue="creation">creation</CI_DateTypeCode>
						</gmd:dateType>
					</gmd:CI_Date>
				</gmd:date>
				</#if>
				<#if datasetReferenceDate.revisionDate?has_content>
				<gmd:date>
					<gmd:CI_Date>
						<gmd:date>
							<gco:Date>${datasetReferenceDate.revisionDate}</gco:Date>
						</gmd:date>
						<gmd:dateType>
							<CI_DateTypeCode xmlns="http://www.isotc211.org/2005/gmd" codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#CI_DateTypeCode" codeListValue="revision">revision</CI_DateTypeCode>
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
					     <gco:CharacterString>${uri.code}</gco:CharacterString>
					</gmd:code>
				   </gmd:MD_Identifier>
				<#else>
					<gmd:RS_Identifier>
						<gmd:code><gco:CharacterString>${uri.code}</gco:CharacterString></gmd:code>
						<#if uri.codeSpace?has_content>
						<gmd:codeSpace><gco:CharacterString>${uri.codeSpace}</gco:CharacterString></gmd:codeSpace>
						</#if>
						<#if uri.version?has_content>
						<gmd:version><gco:CharacterString>${uri.version}</gco:CharacterString></gmd:version>
						</#if>
					</gmd:RS_Identifier>
				</#if>
				</gmd:identifier>
				</#list>
				</#if>
				<#if citation?has_content>
				<gmd:otherCitationDetails>
					<gco:CharacterString>${citation.authors?join(', ')} (${citation.year}). ${citation.title}. ${citation.publisher} ${citation.doi}</gco:CharacterString>
				</gmd:otherCitationDetails>
				</#if>
			</gmd:CI_Citation>
		</gmd:citation>
		<#include "__abstract.xml.tpl">
		<#if responsibleParties?has_content>
		<#list responsibleParties as responsibleParty>
		<gmd:pointOfContact>
			<gmd:CI_ResponsibleParty>
				<#if responsibleParty.individualName?has_content>
				<gmd:individualName><gco:CharacterString>${responsibleParty.individualName}</gco:CharacterString></gmd:individualName>
				</#if>
				<#if responsibleParty.organisationName?has_content>
				<gmd:organisationName><gco:CharacterString>${responsibleParty.organisationName}</gco:CharacterString></gmd:organisationName>
				</#if>
				<gmd:contactInfo>
					<gmd:CI_Contact>
						<gmd:address>
							<gmd:CI_Address>
								<#if responsibleParty.deliveryPoint?has_content>
								<gmd:deliveryPoint><gco:CharacterString>${responsibleParty.deliveryPoint}</gco:CharacterString></gmd:deliveryPoint>
								</#if>
								<#if responsibleParty.city?has_content>
								<gmd:city><gco:CharacterString>${responsibleParty.city}</gco:CharacterString></gmd:city>
								</#if>
								<#if responsibleParty.administrativeArea?has_content>
								<gmd:administrativeArea><gco:CharacterString>${responsibleParty.administrativeArea}</gco:CharacterString></gmd:administrativeArea>
								</#if>
								<#if responsibleParty.postalCode?has_content>
								<gmd:postalCode><gco:CharacterString>${responsibleParty.postalCode}</gco:CharacterString></gmd:postalCode>
								</#if>
								<#if responsibleParty.country?has_content>
								<gmd:country><gco:CharacterString>${responsibleParty.country}</gco:CharacterString></gmd:country>
								</#if>
								<gmd:electronicMailAddress><gco:CharacterString>
									<#if responsibleParty.email?has_content>
										${responsibleParty.email}
									<#else>
										enquiries@ceh.ac.uk
									</#if>
								</gco:CharacterString></gmd:electronicMailAddress>
							</gmd:CI_Address>
						</gmd:address>
					</gmd:CI_Contact>
				</gmd:contactInfo>
				<gmd:role>
				<#if responsibleParty.role = "rightsHolder" >
					<#assign role='owner'>
				<#else>
					<#assign role=responsibleParty.role >
				</#if>
					<gmd:CI_RoleCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#CI_RoleCode" codeListValue="${role}">${role}</gmd:CI_RoleCode>
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
              <gmd:MD_MaintenanceFrequencyCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_MaintenanceFrequencyCode" codeListValue="${rm.frequencyOfUpdate}">${rm.frequencyOfUpdate}</gmd:MD_MaintenanceFrequencyCode>
            </gmd:maintenanceAndUpdateFrequency>
            </#if>
            <#if rm.note?has_content>
            <gmd:maintenanceNote>
              <gco:CharacterString>${rm.note}</gco:CharacterString>
            </gmd:maintenanceNote>
            </#if>
          </gmd:MD_MaintenanceInformation>
        </gmd:resourceMaintenance>
      </#list>
    </#if>
		<#include "_INSPIREkeywords.xml.tpl">
		<#include "_descriptiveKeywords.xml.tpl">
		<#include "_resourceConstraints.xml.tpl">
		<#if spatialRepresentationTypes?has_content>
		<#list spatialRepresentationTypes as spatialRepresentationType>
		<gmd:spatialRepresentationType>
		<gmd:MD_SpatialRepresentationTypeCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_SpatialRepresentationTypeCode" codeListValue="${spatialRepresentationType}">${spatialRepresentationType}</gmd:MD_SpatialRepresentationTypeCode>
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
					  <gco:Distance uom="urn:ogc:def:uom:EPSG::9001">${spatialResolution.distance}</gco:Distance>
				   </gmd:distance>
				</gmd:MD_Resolution>
			 </gmd:spatialResolution>
			</#if>
		</#list>
		</#if>
		<#if type != 'service'>
		<gmd:language>
		<LanguageCode xmlns="http://www.isotc211.org/2005/gmd" codeList="http://www.loc.gov/standards/iso639-2/php/code_list.php" codeListValue="eng">English</LanguageCode>
		</gmd:language>
		<gmd:characterSet>
		<MD_CharacterSetCode xmlns="http://www.isotc211.org/2005/gmd" codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#MD_CharacterSetCode" codeListValue="utf8">utf8</MD_CharacterSetCode>
		</gmd:characterSet>	
		</#if>
		<#if topicCategories??>
		<#list topicCategories as topicCategory>
      <#if topicCategory.value?has_content>
        <gmd:topicCategory>
          <gmd:MD_TopicCategoryCode>${topicCategory.value}</gmd:MD_TopicCategoryCode>
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
</#escape>
