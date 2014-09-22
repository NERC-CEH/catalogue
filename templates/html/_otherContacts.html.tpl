 <!--
 **metadata and distributor responsibleParties should not show up in this list
 -->

<div id="document-otherResponsibleParties">
<h3><a id="authors"></a>Contacts</h3>
<#if responsibleParties?has_content>
	<#list responsibleParties?sort_by("role") as author> <!-- this needs sorting out-->
		<dl class="dl-horizontal">
		<#if author.role != "Author">

			<dt>${author.role}</dt>
			<dd property="">      

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
			</dd>
		</dl>

		</#if>
	</#list>
</#if>

</div>

	<div class="alert alert-danger alert-dismissible" role="alert">
	<button type="button" class="close" data-dismiss="alert"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>
	This needs some work - metadata point of contact (and possibly distributor) should not be in this list
	</div>
