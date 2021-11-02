<#import "blocks.ftlh" as blocks>
<#import "skeleton.ftlh" as skeleton>
<#import "../functions.ftlh" as func>

<#assign
possiblePopulateLink = getLink('populate')
displayPopulateButton = state == 'published' && possiblePopulateLink.isPresent()
/>


<@skeleton.master "Service Agreement">

    <div id="metadata">
        <div class="container" role="main">
            <#if permission.userCanEdit(id)>
                <div class="row hidden-print" id="adminPanel">
                    <div class="text-right" id="adminToolbar" role="toolbar">
                        <div class="btn-group btn-group-sm">
                            <button type="button"
                                    class="btn btn-default btn-wide edit-control"
                                    data-document-type="service-agreement">
                                Edit
                            </button>
                        </div>
                            <#if displayPopulateButton>
                                <div class="btn-group btn-group-sm">
                                    <form action="${possiblePopulateLink.get().href}" method="post">
                                        <input type="submit" value="${possiblePopulateLink.get().title}"${possiblePopulateLink.get().title}" class="btn btn-default btn-wide">
                                    </form>
                                </div>
                            </#if>
                        <div class="btn-group btn-group-sm">
                            <button type="button"
                                    class="btn btn-default btn-wide edit-control"
                                    data-document-type="service-agreement">
                                Publish
                            </button>
                        </div>
                        </div>
                    </div>
                </div>
            </#if>

            <h1>Service Agreement</h1>
            <b>* IMPORTANT * ONCE A DOI HAS BEEN ISSUED,
                THESE DETAILS CANNOT BE CHANGED</b>
            <h1>Environmental Information Data Centre - Service Agreement</h1>
            <p>For more information/guidance about this document see <a href="https://eidc.ac.uk/support/agreement">https://eidc.ac.uk/support/agreement</a>
            </p>
            <h2>The Agreement</h2>
            <p>As a depositor you must consent to this service agreement which establishes the terms and conditions of
                use of your data. This document sets out your rights and responsibilities as depositor and ours as the
                data distributor.
                This agreement authorises the EIDC to preserve and to distribute the data under the terms specified in
                this document. To do so, the EIDC will store the data and, where necessary, duplicate and transform
                copies.</p>
            <h3>Our responsibilities:</h3>
            <ul>
                <li>We will take every care to preserve the integrity of the data and protect it from loss or damage
                </li>
                <li>We will make the data publicly available, subject to any conditions specified in this Service
                    Agreement.
                </li>
                <li>Unless otherwise requested, we will issue a Digital Object Identifier (DOI) to facilitate the proper
                    citation of the dataset.
                </li>
                <li>We will ask users of the data to agree to an end user licence.</li>
                <li>We require users of the data to acknowledge and cite sources when re-using data.</li>
                <li>We will only process personal information in connection with long-term archiving services, in
                    accordance with our <a href="https://eidc.ac.uk/policies/privacy">Privacy Notice</a></li>
            </ul>
            <h3>Your responsibilities:</h3>
            <ul>
                <li>You provide assurances that you are entitled to deposit the data resource in the EIDC and that the
                    agreement of all parties who may have an interest in the resource has been obtained.
                </li>
                <li>You confirm that the resource is not already available elsewhere.</li>
                <li>You agree to supply sufficient supporting information (metadata) to enable discovery, management and
                    reuse of the data.
                </li>
                <li>You ensure that in the case of research data with human subjects that consents collected are
                    ethically and legally appropriate and sufficient to allow deposit of the resource.
                </li>
                <li>You agree to notify the EIDC promptly of any copyright, confidentiality, privacy, data protection,
                    defamation or similar issues pertaining to the data.
                </li>
                <li>You guarantee that nothing in the resource or supporting information contravenes the UK data
                    protection laws or any other UK law (e.g., the Wildlife and Countryside Act 1981).
                </li>
                <li>You agree to notify the EIDC if it is subsequently found that the data resource is no longer fit for
                    purpose (e.g. errors are found in the dataset).
                </li>
            </ul>
            <@displayData "" title/>
            <@displayData "Deposit reference" depositReference/>
            <@displayData "Depositor name" depositorName/>
            <@displayData "Depositor contact details" depositorContactDetails/>
            <@displayData "For the EIDC name" eidcName/>
            <@displayData "For the EIDC contact details" eidcContactDetails/>
            <h3>Data identification and citation</h3>
            <@displayAuthors/>
            <h3>Policies & legislation</h3>
            <@displayData "Other Policies or Legislation" otherPoliciesOrLegislation/>
            <h3>The data</h3>
            <@displayFiles/>
            <@displayData "Transfer method" transferMethod/>
            <@displayRelatedDataHoldings/>
            <@displayDataCategory/>
            <h3>Supporting documentation</h3>
            <@supportingDocuments/>
            <@displayData "Content Included" contentIncluded/>
            <h3>Data retention</h3>
            <#if policyExceptions??> <p>Policy Exceptions: ${policyExceptions}</p> </#if>
            <@displayData "Policy Exceptions" policyExceptions/>
            <h3>Availability and access</h3>
            <@displayData "Availability" availability/>
            <@displayData "Specific Requirements" specificRequirements/>
            <@displayData "Other services required" otherServicesRequired/>
            <h3>Licensing and IPR</h3>
            <@displayEndUserLicence/>
            <@displayOwnerOfIpr/>
            <@displayData "Use constraints" useConstraints/>
            <h3>Superseding existing data (if applicable)</h3>
            <@displayData "Metadata Id" supersededMetadataId/>
            <@displayData "Reason" supersededReason/>
            <h3>Miscellaneous</h3>
            <@displayData "Other Info" otherInfo/>
            <h3>Discovery metadata</h3>
            <@displayData "Description" description/>
            <@displayData "Lineage" lineage/>
            <@displayKeywords/>
            <@displayArea/>
        </div>
    </div>
</@skeleton.master>

<#macro displayData title object=''>
    <#if object!=''> <p>${title}: ${object}</p> </#if>
</#macro>

<#macro displayAuthors>
    <#if authors?has_content && authors?size gt 0 >
        <div id="document-authors">
            <p id="authors"><b>Author<#if authors?size gt 1>s</#if></b></p>

            <#list authors>
                <#assign ownersMaxSize = 30 >
                <#assign moreOwners = authors?size-ownersMaxSize >
                <#items as author>
                    <#assign rpClass="responsibleParty">
                    <#if author?counter gt ownersMaxSize>
                        <#assign rpClass="responsibleParty responsiblePartyCollapse collapse">
                    </#if>

                    <div class="${rpClass}">
                        <@func.displayContact author false false true />
                    </div>

                </#items>

                <#if authors?size gt ownersMaxSize >
                    <p>
                        <small>
                            <a href=".responsiblePartyCollapse" class="moreAuthors collapsed" data-toggle="collapse"
                               aria-controls="responsiblePartyCollapse" area-expanded="false">
                                <span>${moreOwners} more authors. </span>Show
                            </a>
                        </small>
                    </p>
                </#if>
            </#list>
        </div>
    </#if>
</#macro>

<#macro displayFiles>
    <#if files?has_content && files?size gt 0 >
        <div id="document-files">
            <p id="files"><b>Files<#if files?size gt 1>s</#if></b></p>

            <#list files>
                <#assign filesMaxSize = 30 >
                <#assign moreFiles = files?size-filesMaxSize >
                <#items as file>
                    <#assign fileClass="file">
                    <#if file?counter gt filesMaxSize>
                        <#assign fileClass="file fileCollapse collapse">
                    </#if>

                    <div class="${fileClass}">
                        <#if file.name?has_content>
                            <div>
                                <p>Name: ${file.name}</p>
                            </div>
                        </#if>
                        <#if file.format?has_content>
                            <div>
                                <p>Format: ${file.format}</p>
                            </div>
                        </#if>
                        <#if file.size?has_content>
                            <div>
                                <p>Size: ${file.size}</p>
                            </div>
                        </#if>
                    </div>

                </#items>

                <#if files?size gt filesMaxSize >
                    <p>
                    <small>
                    <#if file.name?has_content>
                        <div>
                            <p>Name: ${file.name}</p>
                        </div>
                    </#if>
                    <#if file.format?has_content>
                        <div>
                            <p>Format: ${file.format}</p>
                        </div>
                    </#if>
                    <#if file.size?has_content>
                        <div>
                            <p>Size: ${file.size}</p>
                        </div>
                    </#if>
                    <a href=".fileCollapse" class="moreFiles collapsed" data-toggle="collapse"
                       aria-controls="filePartyCollapse" area-expanded="false">
                        <span>${moreFiles} more files. </span>Show
                    </a>
                    </small>
                    </p>
                </#if>
            </#list>
        </div>
    </#if>
</#macro>

<#macro displayRelatedDataHoldings>
    <#if relatedDataHoldings?? && relatedDataHoldings?has_content>
        <h3>Related Data Holdings</h3><br>
        <dl>
            <#list relatedDataHoldings as dataHolding>
                <dt>Title: ${dataHolding.title}</dt>
                <dd>URL: ${dataHolding.href}</dd><br>
            </#list>
        </dl>
    </#if>
</#macro>

<#macro displayDataCategory>
    <#if dataCategory?? && dataCategory?has_content>
        <h3>Data Category</h3>
        <#if dataCategory.uri?has_content>
            <a href="${dataCategory.uri}" target="_blank" rel="noopener noreferrer">${dataCategory.value}</a>
        <#else>
            <span>${dataCategory.value}</span>
        </#if>
    </#if>
</#macro>

<#macro supportingDocuments>
    <#if supportingDocumentNames?? && supportingDocumentNames?has_content>
        <br><p class="supportingDocumentNames">
        <b>Supporting Documents</b><br>
        <#list supportingDocumentNames as name>
            ${name}<#sep><br></#sep>
        </#list>
        </p>
    </#if>
</#macro>

<#macro displayEndUserLicence>
    <#if endUserLicence?has_content>
          <#if endUserLicence.code == 'license'>
              <p class="licenceText">
                  <#assign licenceText = endUserLicence.value >
                  <#if endUserLicence.uri?has_content><a rel="license" href="${endUserLicence.uri}"></#if>
                      <#if licenceText?contains("Open Government Licence")>
                          <img class="ogl-logo" src='/static/img/ogl_16.png' alt='OGL'>
                      <#else>
                          <#if endUserLicence.value?has_content>${endUserLicence.value}</#if>
                      </#if>
                  <#if endUserLicence.uri?has_content></a></#if>
              </p>
              <div class="divider"></div>
          </#if>
    </#if>
</#macro>

<#macro displayOwnerOfIpr>
    <#if ownersOfIpr?has_content &&ownersOfIpr?size gt 0 >
        <dt id="pointOfContact">Owner of IPR</dt>
        <dd class="descriptive-keywords">
            <#list ownersOfIpr>
                <#items as owner>
                    <div class="responsibleParty">
                        <@func.displayContact owner false false true />
                    </div>
                </#items>
            </#list>
        </dd>
    </#if>
</#macro>

<#macro displayKeywords>
    <#if allKeywords?has_content>
        <dt>Keywords</dt>
        <dd class="descriptive-keywords">
            <#list allKeywords?sort_by("value") as keyword>
                <span>
                 <#if keyword.uri?has_content>
                    <a href="${keyword.uri}" target="_blank" rel="noopener noreferrer">${keyword.value?trim}</a><#if keyword_has_next>,&nbsp;</#if>
                 <#else>
                    ${keyword.value?trim}<#if keyword_has_next>,&nbsp;</#if>
                 </#if>
                </span>
            </#list>
        </dd>
    </#if>
</#macro>

<#macro displayArea>
    <#if areaOfStudy??>
        <div id="section-extent">
            <dl>
                <dt>Study area</dt>
                <dd>
                    <div id="studyarea-map">
                        <#list areaOfStudy as extent>
                            <span content="${extent.wkt}" datatype="geo:wktLiteral"/>
                        </#list>
                    </div>
                </dd>
            </dl>
        </div>
    </#if>
</#macro>
