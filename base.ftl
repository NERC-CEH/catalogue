<#macro stdObjProps>
    <@orNilReason type="gml:description" x=getDescription()! nillable=false>
        <gml:description>${getDescription()}</gml:description>
    </@orNilReason>
    <#if (getMetadata().fileIdentifier)??>
        <gml:identifier codeSpace="${ukeofNamespace}">${getMetadata().fileIdentifier}</gml:identifier>
    </#if>
    <#if getName()?has_content>
        <gml:name>${getName()}</gml:name>
    </#if>
	<#list getAlternativeNames() as alternativeName>
		<gml:name>${alternativeName}</gml:name>
	</#list>
</#macro>

<#macro inspireId>
    <ef:inspireId>
		<base:Identifier>
			<base:localId>${getMetadata().fileIdentifier}</base:localId>
			<base:namespace>${ukeofNamespace}</base:namespace>
		</base:Identifier>
	</ef:inspireId>
</#macro>

<#macro boundedBy box>
    <#if box?has_content>
        <gml:boundedBy>
            <gml:Envelope>
                <gml:lowerCorner>${box.southBoundLatitude} ${box.westBoundLongitude}</gml:lowerCorner>
                <gml:upperCorner>${box.northBoundLatitude} ${box.eastBoundLongitude}</gml:upperCorner>
            </gml:Envelope>
        </gml:boundedBy>
    <#else>
        <gml:boundedBy nilReason="missing" xsi:nil="true"/>
    </#if>
</#macro>

<#macro responsibleParty rp>
    <ef:responsibleParty>
		<base2:RelatedParty>
			<#if rp.individualName?has_content>
				<base2:individualName><gco:CharacterString>${rp.individualName}</gco:CharacterString></base2:individualName>
			</#if>
			<#if rp.organisationName?has_content>
				<base2:organisationName><gco:CharacterString>${rp.organisationName}</gco:CharacterString></base2:organisationName>
			</#if>
			<#if rp.position?has_content>
				<base2:positionName><gco:CharacterString>${rp.position}</gco:CharacterString></base2:positionName>
			</#if>
			<#if rp.email?has_content>
				<base2:contact>
					<base2:Contact>
						<base2:electronicMailAddress>${rp.email}</base2:electronicMailAddress>
					</base2:Contact>
				</base2:contact>
			</#if>
			<#if rp.role?has_content>
				<#-- link to role vocabulary -->
				<base2:role xlink:title="${rp.role.codeListValue}" xlink:href="${rp.role.codeList}"/>
			</#if>
		</base2:RelatedParty>
	</ef:responsibleParty>
</#macro>

<#macro link type link>
    <#if link.title?has_content>
        <ef:${type} xlink:title="${link.title}" xlink:href="${link.href}"/>
    <#else>
        <ef:${type} xlink:href="${link.href}"/>
    </#if>
</#macro>

<#macro onlineResource onlineResource>
    <#if onlineResource.href?has_content>
        <ef:onlineResource>${onlineResource.href}</ef:onlineResource>
    </#if>
</#macro>

<#macro timePeriod tp id>
    <gml:TimePeriod gml:id="${id}">
        <#if tp.start??>
            <gml:beginPosition>${tp.start}</gml:beginPosition>
        <#else>
            <gml:beginPosition indeterminatePosition="unknown"/>
        </#if>
        <#if tp.end??>
            <gml:endPosition>${tp.end}</gml:endPosition>
        <#else>
            <gml:endPosition indeterminatePosition="unknown"/>
        </#if>
    </gml:TimePeriod>
</#macro>

<#macro observingCapability oc index>
    <ef:observingCapability>
        <ef:ObservingCapability gml:id="oc_${index}">
            <@orNilReason "ef:observingTime" oc.observingTime!>
                <#assign id = "ot_${index}"/>
                <ef:observingTime>
                    <@base.timePeriod oc.observingTime id/>
                </ef:observingTime>
            </@orNilReason>
            <@orNilReason "ef:processType" oc.processType!>
                <@link "processType" oc.processType!/>
            </@orNilReason>
            <@orNilReason "ef:resultNature" oc.resultNature!>
                <@link "resultNature" oc.resultNature!/>
            </@orNilReason>
            <@orNilReason "ef:onlineResource" oc.onlineResource!>
                <@onlineResource oc.onlineResource!/>
            </@orNilReason>
            <@orNilReason type="ef:procedure" x=oc.procedure! nillable=false>
                <@link "procedure" oc.procedure!/>
            </@orNilReason>
            <@orNilReason "ef:featureOfInterest" oc.featureOfInterest!>
                <@link "featureOfInterest" oc.featureOfInterest!/>
            </@orNilReason>
            <@orNilReason type="ef:observedProperty" x=oc.observedProperty! nillable=false>
                <@link "observedProperty" oc.observedProperty!/>
            </@orNilReason>
        </ef:ObservingCapability>
    </ef:observingCapability>
</#macro>

<#macro orNilReason type x nillable=true>
    <#if x?has_content>
        <#nested/>
    <#else>
        <${type} nilReason="missing" <#if nillable>xsi:nil="true"</#if>/>
    </#if>
</#macro>