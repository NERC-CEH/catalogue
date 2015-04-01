<#if temporalExtent?has_content>
<#list temporalExtent as temporal>
<${ns}:extent>
	<gmd:EX_Extent>
		<gmd:temporalElement>
			<gmd:EX_TemporalExtent>
				<gmd:extent>
					<gml:TimePeriod gml:id="EIDC79854">
						<gml:beginPosition>${temporal.begin}</gml:beginPosition>
						<gml:endPosition>${temporal.end}</gml:endPosition>
					</gml:TimePeriod>
				</gmd:extent>
			</gmd:EX_TemporalExtent>
		</gmd:temporalElement>
	</gmd:EX_Extent>
</${ns}:extent>
</#list>
</#if>
<#if boundingBoxes?has_content>
<#list boundingBoxes as boundingBox>
<${ns}:extent>
	<gmd:EX_Extent>
		<gmd:geographicElement>
			<gmd:EX_GeographicBoundingBox>
				<gmd:westBoundLongitude><gco:Decimal>${boundingBox.westBoundLongitude}</gco:Decimal></gmd:westBoundLongitude>
				<gmd:eastBoundLongitude><gco:Decimal>${boundingBox.eastBoundLongitude}</gco:Decimal></gmd:eastBoundLongitude>
				<gmd:southBoundLatitude><gco:Decimal>${boundingBox.southBoundLatitude}</gco:Decimal></gmd:southBoundLatitude>
				<gmd:northBoundLatitude><gco:Decimal>${boundingBox.northBoundLatitude}</gco:Decimal></gmd:northBoundLatitude>
			</gmd:EX_GeographicBoundingBox>
		</gmd:geographicElement>
	</gmd:EX_Extent>
</${ns}:extent>
</#list>
</#if>
<#if supplementalInfo?has_content>
<gmd:supplementalInformation><gco:CharacterString>${supplementalInfo?xml}</gco:CharacterString></gmd:supplementalInformation>
</#if>