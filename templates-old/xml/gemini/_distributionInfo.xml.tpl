<#escape x as x?xml>
<gmd:distributionInfo>
  <gmd:MD_Distribution>
    <#if distributionFormats?has_content>
    <#list distributionFormats as distributionFormat>
    <gmd:distributionFormat>
      <gmd:MD_Format>
        <gmd:name><gco:CharacterString>${distributionFormat.name}</gco:CharacterString></gmd:name>
        <#if distributionFormat.version?has_content && distributionFormat.version?lower_case !='unknown'>
          <gmd:version><gco:CharacterString>${distributionFormat.version}</gco:CharacterString></gmd:version>
        <#else>
          <gmd:version gco:nilReason="unknown"/>
        </#if>
      </gmd:MD_Format>
    </gmd:distributionFormat>
    </#list>
    </#if>
    <#if distributorContacts?has_content>
    <#list distributorContacts as distributorContact>
    <gmd:distributor>
      <gmd:MD_Distributor>
      <gmd:distributorContact>
      <gmd:CI_ResponsibleParty>
        <#if distributorContact.individualName?has_content>
        <gmd:individualName><gco:CharacterString>${distributorContact.individualName}</gco:CharacterString></gmd:individualName>
        </#if>
        <#if distributorContact.organisationName?has_content>
        <gmd:organisationName><gco:CharacterString>${distributorContact.organisationName}</gco:CharacterString></gmd:organisationName>
        </#if>
        <gmd:contactInfo>
          <gmd:CI_Contact>
            <gmd:address>
              <gmd:CI_Address>
                <#if distributorContact.address?has_content>
                <#if distributorContact.address.deliveryPoint?has_content>
                <gmd:deliveryPoint><gco:CharacterString>${distributorContact.address.deliveryPoint}</gco:CharacterString></gmd:deliveryPoint>
                </#if>
                <#if distributorContact.address.city?has_content>
                <gmd:city><gco:CharacterString>${distributorContact.address.city}</gco:CharacterString></gmd:city>
                </#if>
                <#if distributorContact.address.administrativeArea?has_content>
                <gmd:administrativeArea><gco:CharacterString>${distributorContact.address.administrativeArea}</gco:CharacterString></gmd:administrativeArea>
                </#if>
                <#if distributorContact.address.postalCode?has_content>
                <gmd:postalCode><gco:CharacterString>${distributorContact.address.postalCode}</gco:CharacterString></gmd:postalCode>
                </#if>
                <#if distributorContact.address.country?has_content>
                <gmd:country><gco:CharacterString>${distributorContact.address.country}</gco:CharacterString></gmd:country>
                </#if>
                </#if>
                <#if distributorContact.email?has_content>
                <gmd:electronicMailAddress><gco:CharacterString>${distributorContact.email}</gco:CharacterString></gmd:electronicMailAddress>
                </#if>
              </gmd:CI_Address>
            </gmd:address>
          </gmd:CI_Contact>
        </gmd:contactInfo>
        <gmd:role>
          <gmd:CI_RoleCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/Codelist/ML_gmxCodelists.xml#CI_RoleCode" codeListValue="distributor">distributor</gmd:CI_RoleCode>
        </gmd:role>
      </gmd:CI_ResponsibleParty>
      </gmd:distributorContact>
      </gmd:MD_Distributor>
    </gmd:distributor>
    </#list>
    </#if>
    <#if onlineResources??>
    <#list onlineResources as onlineResource>
    <gmd:transferOptions>
      <gmd:MD_DigitalTransferOptions>
        <gmd:onLine>
          <gmd:CI_OnlineResource>
            <gmd:linkage><gmd:URL>${onlineResource.url}</gmd:URL></gmd:linkage>
            <gmd:name><gco:CharacterString>${onlineResource.name}</gco:CharacterString></gmd:name>
            <#if onlineResource.description?has_content>
            <gmd:description><gco:CharacterString>${onlineResource.description}</gco:CharacterString></gmd:description>
            </#if>
            <#if (onlineResource.function??) && (
              (onlineResource.function == 'download') ||
              (onlineResource.function == 'information') ||
              (onlineResource.function == 'offlineAccess') ||
              (onlineResource.function == 'order') ||
              (onlineResource.function == 'search')
            )>
            <gmd:function>
              <gmd:CI_OnLineFunctionCode codeList="http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/gmxCodelists.xml#CI_OnLineFunctionCode" codeListValue="${onlineResource.function}">${onlineResource.function}</gmd:CI_OnLineFunctionCode>
            </gmd:function>
            </#if>
          </gmd:CI_OnlineResource>
        </gmd:onLine>
      </gmd:MD_DigitalTransferOptions>
    </gmd:transferOptions>
    </#list>
    </#if>
  </gmd:MD_Distribution>
</gmd:distributionInfo>
</#escape>
