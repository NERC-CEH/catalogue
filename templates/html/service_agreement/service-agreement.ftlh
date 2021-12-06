<#import "../blocks.ftlh" as blocks>
<#import "../skeleton.ftlh" as skeleton>
<#import "../../functions.ftlh" as func>

<@skeleton.master "Service Agreement">

    <div id="metadata" class="serviceAgreement">
        <div class="container" role="main">
            <#if permission.userCanEdit(id)>
                <div class="row hidden-print" id="adminPanel">
                    <div class="text-right" id="adminToolbar" role="toolbar">
                        <button type="button" class="btn btn-default btn-wide edit-control" data-document-type="service-agreement">
                            Edit
                        </button>
                        <#list getModelLinks() as link>
                            <#if link.rel == 'history'>
                                <a href="${link.href}" class="btn btn-default btn-wide">${link.title}</a>
                            <#else>
                                <form action="${link.href}" method="post">
                                    <input type="submit" value="${link.title}" class="btn btn-default btn-wide">
                                </form>
                            </#if>
                        </#list>
                    </div>
                </div>
            </#if>

            <#if permission.userCanEdit(id)>
                <#include "_serviceAgreementQualityAlert.ftlh">
            </#if>

            <div class="title">
                <div class="logo"></div>
                <h1>EIDC Deposit Agreement for ${title}</h1>
            </div>

            <div class="preamble">
                <p>As a depositor you must consent to this service agreement which establishes the terms and conditions of use of your data.  This document sets out your rights and responsibilities as depositor and ours as the data distributor.</p><p>This agreement authorises the EIDC to preserve and to distribute the data under the terms specified in this document.  To do so, the EIDC will store the data and, where necessary, duplicate and transform copies.</p>
                <div class="indent--1">
                    <h3>Our responsibilities:</h3>
                    <ul>
                        <li>We will take every care to preserve the integrity of the data and protect it from loss or damage.</li>
                        <li>We will make the data publicly available, subject to any conditions specified in this Agreement.</li>
                        <li>Unless otherwise requested, we will issue a Digital Object Identifier (DOI) to facilitate the proper citation of the dataset. </li>
                        <li>We will ask users of the data to agree to an end user licence.</li>
                        <li>We require users of the data to acknowledge and cite sources when re-using data.</li>
                        <li>We will only process personal information in connection with long-term archiving services, in accordance with our <a href="https://eidc.ac.uk/policies/privacy" target="_blank" rel="noopener noreferrer" >Privacy Notice</a></li>
                    </ul>
                    <h3>Your responsibilities:</h3>
                    <ul>
                        <li>You provide assurances that you are entitled to deposit the data resource in the EIDC and that the agreement of all parties who may have an interest in the resource has been obtained.</li>
                        <li>You confirm that the resource is not already available elsewhere.</li>
                        <li>You agree to supply sufficient supporting information (metadata) to enable discovery, management and reuse of the data. </li>
                        <li>You ensure that in the case of research data with human subjects that consents collected are ethically and legally appropriate and sufficient to allow deposit of the resource.</li>
                        <li>You agree to notify the EIDC promptly of any copyright, confidentiality, privacy, data protection, defamation or similar issues pertaining to the data.</li>
                        <li>You guarantee that nothing in the resource or supporting information contravenes the UK data protection laws or any other UK law (e.g., the Wildlife and Countryside Act 1981).</li>
                        <li>You agree to notify the EIDC if it is subsequently found that the data resource is no longer fit for purpose (e.g. if errors are found in the dataset).</li>
                    </ul>
                </div>
            </div>

            <div class="section">
                <@displayData "Deposit reference" depositReference/>
                <#if depositorName!=''>
                    <div class="keyvalue">
                        <div class="key">Depositor</div>
                        <div class="value">
                            ${depositorName}
                            <#if depositorContactDetails!=''><span>(${depositorContactDetails})</span></#if>
                        </div>
                    </div>
                </#if>
                <#if eidcName!=''>
                    <div class="keyvalue">
                        <div class="key">For the EIDC</div>
                        <div class="value">
                            ${eidcName}
                            <#if eidcContactDetails!=''><span>(${eidcContactDetails})</span></#if>
                        </div>
                    </div>
                </#if>
            </div>

            <div class="section section--1">
                <h2>Data identification and citation</h2>
                <p><strong class="text-red"><i class="fas fa-exclamation-triangle" ></i> PLEASE NOTE: ONCE A DOI HAS BEEN ISSUED, THE INFORMATION PROVIDED IN THIS SECTION CANNOT BE CHANGED</strong></p>

                <h3>Title of data resource</h3>
                <p>${title}</p>

                <div class="authors">
                    <h3>Authors</h3>
                    <#if authors?has_content && authors?size gt 0 >
                         <@displayAuthors/>
                    <#else>
                    <div class="error">NO AUTHORS LISTED</div>
                    </#if>
                </div>
            </div>

            <div class="section section--2">
                <h2>Policies & legislation</h2>
                <p>All environmental data deposited into the EIDC are subject to the requirements of the NERC Data Policy.</p>
                <p>By depositing data, you confirm that the data is compliant with the provisions of UK data protection laws.</p>
                <p>Data and supporting documentation should not contain names, addresses or other personal information relating to 'identifiable natural persons'.  Discovery metadata (the catalogue record) may contain names and contact details of the authors of this data (<a href="#">see our policy on retention and use of personal data</a>).</p>
                <dl class="dl-horizontal">
                    <@displayData "Other policies/legislation that applies:" otherPoliciesOrLegislation/>
                </dl>
            </div>

            <div class="section section--3">
                <h2>Data and supporting documentation</h2>
                <#if files?has_content && files?size gt 0 >
                    <@displayFiles/>
                </#if>
                
                <#if supportingDocumentNames?? && supportingDocumentNames?has_content>
                    <@displaySupportingDocuments/>
                </#if>
                <@displayData "Content Included" contentIncluded/>

                <@displayData "Agreed transfer method for data and supporting documents" transferMethod/>
                <!--@displayData "Data category" dataCategory.value/>
                <@displayRelatedDataHoldings/>-->
            </div>


            <#if policyExceptions??>
            <div class="section section--5">
                <h2>Data retention</h2>
                <p>Policy Exceptions: ${policyExceptions}</p> 
            </div>
            </#if>

            <#if availability?? || specificRequirements?? || otherServicesRequired??>
                <div class="section section--6">
                    <h2>Availability and access</h2>
                    <dl class="dl-horizontal">
                        <@displayData "Availability" availability/>
                        <@displayData "Specific Requirements" specificRequirements/>
                        <@displayData "Other services required" otherServicesRequired/>
                    </dl>
                </div>
            </#if>

            <#if endUserLicence?has_content || (ownersOfIpr?has_content &&ownersOfIpr?size gt 0)>
                <div class="section section--7">
                    <h2>Licensing and IPR</h2>
                    <@displayEndUserLicence/>
                    <@displayOwnerOfIpr/>
                </div>
            </#if>

            <#if useConstraints?? || supersededMetadataId?? || supersededReason??>
                <div class="section section--8">
                    <@displayData "Use constraints" useConstraints/>
                    <@displayData "Metadata Id" supersededMetadataId/>
                    <@displayData "Reason" supersededReason/>
                </div>
            </#if>

            <div class="section section--9">
                <h2>Miscellaneous</h2>
                <@displayData "Other Info" otherInfo/>
            </div>

            <#if description?has_content || lineage?has_content || allKeywords?has_content || areaOfStudy??>
                <div class="section section--metadata">
                    <h2>Discovery metadata</h2>
                    <@displayData "Description" description/>
                    <@displayData "Lineage" lineage/>
                    <@displayKeywords/>
                    <@displayArea/>
                </div>
            </#if>

            <#if permission.userCanEdit(id)>
                <#include "_serviceAgreementQualityReport.ftlh">
            </#if>

        </div>
    </div>
</@skeleton.master>


<#-- Freemarker macros -->
<#macro displayData key value=''>
    <#if value!=''>
    <div class="keyvalue">
        <div class="key">${key}</div>
        <div class="value">${value}</div>
    </div>
    </#if>
</#macro>

<#macro displayAuthors>
    <#list authors>
        <ul>
            <li>Authors are listed below in the order in which they will appear in the citation</li>
            <li>Details of the authors listed below will be published in a public data catalogue and held in EIDC systems. UK law requires us to inform all individuals that they are being proposed as an author. A current, valid email address (or phone number) for all living authors is therefore required.  Those without valid contact details are not eligible for authorship</li>
            <li>Please see our <a href="http://eidc.ceh.ac.uk/policies/privacy" target="_blank" rel="noopener noreferrer" >Privacy Notice</a> for further information</li>
            </ul>
        <div class="tbl tbl--collapse tbl--authors">
        <table class="">
        <thead>
            <tr>
                <th>Author name</th>
                <th>Affiliation</th>
                <th>Email address</th>
                <th>ORCiD</th>
            </tr>
        </thead>
        <tbody>
        <#items as author>
            <tr>
                <td><#if author.individualName?has_content >${author.individualName}<#else><span class="error">missing</span></#if></td>
                <td><#if author.organisationName?has_content >${author.organisationName}<#else><span class="error">missing</span></#if></td>
                <td><#if author.email?has_content>${author.email}<#else><span class="error">missing</span></#if></td>
                <td><#if author.nameIdentifier?has_content >${author.nameIdentifier}<#else><small class="text-muted">Not provided</small></#if></td>
            </tr>
        </#items>
        </tbody>
        </table>
        </div>
    </#list>
</#macro>

<#macro displayFiles>
    <#list files>
    <div class="tbl tbl--collapse tbl--files">
        <table>
            <thead>
                <tr>
                    <th>Filename </th>
                    <th>Format</th>
                    <th>Size</th>
                </tr>
            </thead>
            <tbody>                    
            <#items as file>
                <tr>
                    <td><#if file.name?has_content>${file.name}<#else><span class="error">missing</span></#if></td>
                    <td><#if file.format?has_content>${file.format}<#else><span class="error">missing</span></#if></td>
                    <td><#if file.size?has_content>${file.size}<#else><span class="error">missing</span></#if></td>
                </tr>
            </#items>
            </tbody>
        </table>
    </div>
    </#list>
</#macro>

<#macro displayRelatedDataHoldings>
    <#if relatedDataHoldings?? && relatedDataHoldings?has_content>
        <h3>Related Data Holdings</h3>
        <#list relatedDataHoldings as holding>
            <#if holding.href?has_content>
                <#assign holdinglabel = holding.href>
                <#if holding.title?has_content>
                    <#assign holdinglabel = holding.title>
                </#if>
                <a href="${holding.href}">${holdinglabel}</a>
            <#elseif holding.title?has_content>
                ${holding.title}
            </#if>
        </#list>
    </#if>
</#macro>

<#macro displaySupportingDocuments>
    <p class="supportingDocumentNames">
    <b>Supporting Documents</b><br>
    <#list supportingDocumentNames as name>
        ${name}<#sep><br></#sep>
    </#list>
    </p>
</#macro>

<#macro displayEndUserLicence>
    <#if endUserLicence?has_content>
        <#if endUserLicence.code == 'license'>
            <#assign licenceText = endUserLicence.value >
            <#if endUserLicence.uri?has_content>
                <a rel="license" href="${endUserLicence.uri}">${licenceText}</a>
            <#else>
                ${licenceText}
            </#if>
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
                        <@func.displayContact owner false true true />
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
                     <a href="${keyword.uri}" target="_blank"
                        rel="noopener noreferrer">${keyword.value?trim}</a><#if keyword_has_next>,&nbsp;</#if>
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
