<div id="section-authors">
<h3><a id="authors"></a>Authors</h3>
<#if responsibleParties?has_content><!-- this needs sorting out-->
	<#list responsibleParties as author> 
		
		<#if author.role == "Author">

			<div property="dct:contributor">      

				<#if author.email?has_content>
					<#if author.individualName?has_content>
						<a href="mailto:${author.email}&subject=RE:${title}">${author.individualName}</a><br>
						<#if author.organisationName?has_content>
							<span>${author.organisationName}</span><br>
						</#if>
					<#else>
						<a href="mailto:${author.email}&subject=RE:${title}">${author.organisationName}</a><br>
					</#if>
				<#else>
					<#if author.individualName?has_content>
					  <span>${author.individualName}</span><br>
					</#if>
					<#if author.organisationName?has_content>
						<span>${author.organisationName}</span><br>
					</#if>
				</#if>

				<!--I want to add the address here too-->
				<!--<address class="hidden-xs">
				Address line 1<br>
				Address line 2<br>
				etc<br>
				</address>-->
			</div>

		</#if>
	</#list>
</#if>
</div>
