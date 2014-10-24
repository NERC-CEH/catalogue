<div id="section-metadata">
	<h3><a id="metadata"></a>Metadata [PLACEHOLDER TEXT]</h3>
	<dl class="dl-horizontal">
		<dt><span class="element" data-content="Unique identifier for this record" data-trigger="hover" data-placement="right">Record identifier</span></dt>
		<dd property="dct:identifier iso:fileIdentifier" >${id}</dd>
		<dt>Record created by</dt>
		<dd property="dct:creator"><address class="placeholderText"><span>
		EIDC Hub<br>
		Lancaster Environment Centre<br>
		Bailrigg<br>
		Lancaster<br>
		LA1 4AP<br></span>
		<a href="mailto:eidc@ceh.ac.uk">eidc@ceh.ac.uk</a></dd>
		</address>
		
		<dt>Record last updated</dt>
		<dd>
			<#setting date_format = 'yyyy-MM-dd'>
			${metadataDate}
		</dd>	

		
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
