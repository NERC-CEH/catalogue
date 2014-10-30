<div id="section-metadata">
	<h3><a id="metadata"></a>Metadata</h3>
	<dl class="dl-horizontal">
		<dt><span class="element" data-content="Unique identifier for this record" data-trigger="hover" data-placement="right">Record identifier</span></dt>
		<dd property="dct:identifier iso:fileIdentifier" >${id}</dd>
		<#if metadataPointsOfContact?has_content>
		<dt>Record created by</dt>
			<#list metadataPointsOfContact as metadataPointOfContact>
				<dd>      

					<#if metadataPointOfContact.email?has_content>
						<#if metadataPointOfContact.individualName?has_content>
							<a href="mailto:${metadataPointOfContact.email}&subject=RE:${title}">${metadataPointOfContact.individualName}</a><br>
							<#if metadataPointOfContact.organisationName?has_content>
								<span>${metadataPointOfContact.organisationName}</span><br>
							</#if>
						<#else>
							<a href="mailto:${metadataPointOfContact.email}&subject=RE:${title}">${metadataPointOfContact.organisationName}</a><br>
						</#if>
					<#else>
						<#if metadataPointOfContact.individualName?has_content>
						  <span>${metadataPointOfContact.individualName}</span><br>
						</#if>
						<#if metadataPointOfContact.organisationName?has_content>
							<span>${metadataPointOfContact.organisationName}</span><br>
						</#if>
					</#if>
									
					<#if metadataPointOfContact.address?has_content>
						<address class="hidden-xs">
							<#if metadataPointOfContact.address.deliveryPoint?has_content>${metadataPointOfContact.address.deliveryPoint}<br></#if>
							<#if metadataPointOfContact.address.city?has_content>${metadataPointOfContact.address.city}<br></#if>
							<#if metadataPointOfContact.address.administrativeArea?has_content>${metadataPointOfContact.address.administrativeArea}<br></#if>
							<#if metadataPointOfContact.address.postalCode?has_content>${metadataPointOfContact.address.postalCode}<br></#if>
							<#if metadataPointOfContact.address.country?has_content>${metadataPointOfContact.address.country}</#if>
						</address>
					</#if>
				</dd>
			</#list>
		</#if>	
				
		<dt>Record last updated</dt>
		<dd>${metadataDate}</dd>	

		
		<#if metadataStandardName?has_content>
		<dt>Metadata standard</dt>
		<dd>
			${metadataStandardName}
			
			<#if metadataStandardVersion?has_content>
			version ${metadataStandardVersion}
			</#if>
		</dd>
		</#if>
		
		<dt> </dt>
		<dd><a href="?format=GeminiWhatever">View complete GEMINI 2 metadata record (xml)</a></dd>
	</dl>
</div>



	
