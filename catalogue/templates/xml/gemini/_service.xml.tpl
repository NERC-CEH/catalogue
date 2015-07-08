<#escape x as x?xml>
<#if service?has_content>
<srv:serviceType>
	<gco:LocalName>${service.type}</gco:LocalName>
</srv:serviceType>
<#list service.versions as version>
<srv:serviceTypeVersion>
	<gco:CharacterString>${version}</gco:CharacterString>
</srv:serviceTypeVersion>
</#list>
<#include "_extent.xml.tpl">
<#list service.coupledResources as coupledResource>	
<srv:coupledResource>
	<srv:SV_CoupledResource>
		<srv:operationName>
			<gco:CharacterString>${coupledResource.operationName}</gco:CharacterString>
		</srv:operationName>
		<srv:identifier>
			<gco:CharacterString>${coupledResource.identifier}</gco:CharacterString>
		</srv:identifier>
	</srv:SV_CoupledResource>
</srv:coupledResource>
</#list>
<srv:couplingType>
	<srv:SV_CouplingType codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#SV_CouplingType" codeListValue="${service.couplingType}">${service.couplingType}</srv:SV_CouplingType>
</srv:couplingType>
<#list service.containsOperations as containsOperation>	
<srv:containsOperations>
	<srv:SV_OperationMetadata>
		<srv:operationName>
			<gco:CharacterString>${containsOperation.operationName}</gco:CharacterString>
		</srv:operationName>
		<srv:DCP>
		<srv:DCPList codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#DCPList" codeListValue="${containsOperation.platform}">${containsOperation.platform}</srv:DCPList>
		</srv:DCP>
		<srv:connectPoint>
			<gmd:CI_OnlineResource>
				<gmd:linkage>
					<gmd:URL>${containsOperation.url}</gmd:URL>
				</gmd:linkage>
			</gmd:CI_OnlineResource>
		</srv:connectPoint>
	</srv:SV_OperationMetadata>
</srv:containsOperations>
</#list>
<#list service.coupledResources as coupledResource>
	<srv:operatesOn xlink:href="${coupledResource.identifier}" uuidref="${coupledResource.identifier}"/>
</#list>
</#if>
</#escape>
