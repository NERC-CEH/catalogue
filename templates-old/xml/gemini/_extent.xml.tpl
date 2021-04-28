<#escape x as x?xml>

<#macro temporalPosition type temporal>
	<#if temporal[type]?has_content>
		<gml:${type}Position>${temporal[type]}</gml:${type}Position>
	<#else>
		<gml:${type}Position indeterminatePosition="unknown"/>
	</#if>
</#macro>

<#if temporalExtents?has_content>
<#list temporalExtents as temporal>
<${ns}:extent>
	<gmd:EX_Extent>
		<gmd:temporalElement>
			<gmd:EX_TemporalExtent>
				<gmd:extent>
					<gml:TimePeriod gml:id="EIDC79854${temporal_index}">
						<@temporalPosition 'begin' temporal/>
						<@temporalPosition 'end' temporal/>
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
				<#escape x as x?c>
					<gmd:westBoundLongitude><gco:Decimal>${boundingBox.westBoundLongitude}</gco:Decimal></gmd:westBoundLongitude>
					<gmd:eastBoundLongitude><gco:Decimal>${boundingBox.eastBoundLongitude}</gco:Decimal></gmd:eastBoundLongitude>
					<gmd:southBoundLatitude><gco:Decimal>${boundingBox.southBoundLatitude}</gco:Decimal></gmd:southBoundLatitude>
					<gmd:northBoundLatitude><gco:Decimal>${boundingBox.northBoundLatitude}</gco:Decimal></gmd:northBoundLatitude>
				</#escape>
			</gmd:EX_GeographicBoundingBox>
		</gmd:geographicElement>
	</gmd:EX_Extent>
</${ns}:extent>
</#list>
</#if>
</#escape>