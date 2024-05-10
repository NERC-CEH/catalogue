<#import "../skeleton.ftlh" as skeleton>
<#import "../blocks.ftlh" as blocks>
<#import "../../functions.ftlh" as func>

<@skeleton.master title=title catalogue=catalogues.defaultCatalogue()>
    <#if isHistorical()>
        <#assign mdclass="historical">
    <#else>
        <#assign mdclass="">
    </#if>

    <div id="metadata" class="serviceAgreement ${mdclass}">
        <div class="container" role="main">

            <div class="title">
                <div class="logo"></div>
                <h1>EIDC Deposit Agreement for ${title!"NOT PROVIDED"} (Status: ${getState()?upper_case})</h1>
            </div>

            <#if permission.userCanEditServiceAgreement(id)>
            <div class="text-right">
                <div class="row hidden-print adminPanel" role="toolbar">
                    <#if !isHistorical()>
                        <#if getState() != "pending publication" && getState() != "published">
                           <div class="btn-group btn-group-sm">
                             <button type="button" class="btn btn-default edit-control btn-wide" data-document-type="service-agreement">Edit</button>
                             <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" >
                               <span class="caret"></span>
                               <span class="sr-only">Toggle Dropdown</span>
                             </button>
                             <ul class="dropdown-menu dropdown-menu-right">
                               <li>
                                 <a href="/service-agreement/${id}/publication">
                                    <i class="fa-solid fa-file"></i>
                                    Service agreement publication status
                                 </a>
                               </li>
                             </ul>
                           </div>
                        </#if>

                        <#list getModelLinks() as link>
                            <#if link.rel == 'history'>
                                <a href="${link.href}" class="btn btn-default btn-wide btn-sm">${link.title}</a>
                            <#else>
                                <form action="${link.href}" method="post">
                                    <input type="submit" class="btn btn-default btn-wide btn-sm" value="${link.title}">
                                </form>
                            </#if>
                        </#list>
                    <#elseif  isHistorical()>
                        <#list getModelLinks() as link>
                            <#if link.title == 'History'>
                                <a href="${link.href}" class="btn btn-default btn-wide btn-sm">${link.title}</a>
                            </#if>
                        </#list>
                    </#if>
                </div>
            </div>
            </#if>

            <#if permission.userCanEditServiceAgreement(id) && !isHistorical()>
                <#include "_serviceAgreementQualityReport.ftlh">
            </#if>

            <#if isHistorical()>
                <h1 class="text-red">Historic version<br><small><strong>IMPORTANT</strong> This is a historic version of this Service Agreement</small></h1>
            </#if>

            <div class="terms">
                <p>This document sets out your rights and responsibilities as depositor and ours as the data distributor.</p>
                <p>As a depositor you must consent to this service agreement which establishes the terms and conditions of use of your data.</p>
                <p>This agreement authorises the EIDC to preserve and to distribute the data under the terms specified in this document.  To do so, the EIDC will store the data and, where necessary, duplicate and transform copies.</p>
                <p>All environmental data deposited into the EIDC are subject to the requirements of the NERC Data Policy.</p>
                <p>By depositing data, you confirm that the data is compliant with the provisions of UK data protection laws.</p>

                <div class="indent--1">
                    <h3>Our responsibilities:</h3>
                    <ul>
                        <li>We will take every care to preserve the integrity of the data and protect it from loss or damage.</li>
                        <li>We will make the data publicly available, subject to any conditions specified in this Agreement.</li>
                        <li>Unless otherwise requested, we will issue a Digital Object Identifier (DOI) to facilitate the proper citation of the dataset. Such data will be kept in perpetuity. </li>
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

                    <#if policyExceptions??>
                        <h3>Exceptions to the general policy are listed below:</h3>
                        <ul>
                            <li>${policyExceptions}</li>
                            <li>otherPoliciesOrLegislation</li>
                        </ul>
                    </#if>
                </div>
            </div>

            <div class="contactDetails">
                <div class="keyvalue">
                    <div class="key">Deposit reference</div>
                    <div class="value"><span>${depositReference!"NOT PROVIDED"}</span></div>
                </div>
                <#if depositorName?? && depositorName!=''>
                    <div class="keyvalue">
                        <div class="key">Depositor</div>
                        <div class="value">
                            ${depositorName}
                            <#if depositorContactDetails!=''><span>(${depositorContactDetails})</span></#if>
                        </div>
                    </div>
                </#if>
                <#if eidcName?? && eidcName!=''>
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
                <p><strong class="text-red"><i class="fa-solid fa-triangle-exclamation" ></i> PLEASE NOTE: ONCE A DOI HAS BEEN ISSUED, THE INFORMATION PROVIDED IN THIS SECTION CANNOT BE CHANGED</strong><br>&nbsp;</p>
                <h4><strong>Title of data resource:</strong> ${title}</h4>

                <div class="authors">
                    <h3>Authors</h3>
                    <p>This information will be used in discovery metadata for this dataset (the catalogue record).  <a href="#">See our policy on retention and use of personal data</a>.</p>

                    <#if authors?has_content && authors?size gt 0 >
                        <@displayAuthors/>
                    <#else>
                    <div class="alert alert--error">NO AUTHORS LISTED</div>
                    </#if>
                </div>
            </div>

            <div class="section section--3">
                <h2>Data and supporting documentation</h2>
                <p>Data and supporting documentation should not contain names, addresses or other personal information relating to 'identifiable natural persons' (<a href="#">see our policy on retention and use of personal data</a>).</p>

                <#if files?has_content && files?size gt 0 >
                    <@displayFiles/>
                </#if>

                <@displayData "File naming convention" fileNamingConvention/>

                <@displayData "Number of files to be deposited" fileNumber/>

                <#if supportingDocs?has_content && supportingDocs?size gt 0 >
                    <div class="keyvalue">
                        <div class="key">Supporting documents</div>
                        <div class="value">
                            <#list supportingDocs as doc>
                                <div class="supportingDoc">
                                    <div class="supportingDoc__name">
                                        <#if doc.name?? && doc.format??>
                                            <i>${doc.name}.${doc.format}</i>
                                        <#else>
                                           <span class="alert alert--error">missing</span>
                                        </#if>
                                    </div>
                                    <div class="supportingDoc__content">
                                        <ul>
                                            <#list doc.content![] as kind>
                                                <li>${doc.contentTypeToText(kind)}</li>
                                            <#else>
                                                <span class="alert alert--error">missing</span>
                                            </#list>
                                        </ul>
                                    </div>
                                </div>
                            </#list>
                        </div>
                    </div>
                </#if>

                <#if dataCategory?has_content>
                    <@displayData "Data category" dataCategory.value/>
                </#if>

                <@displayData "Agreed transfer method for data and supporting documents" transferMethod/>

            </div>


            <div class="section section--6">
                <h2>Availability, access and licensing</h2>
                <div class="keyvalue">
                    <div class="key">Availability</div>
                    <div class="value">
                        <#if availability??>
                            ${availability}
                        <#else>
                            <p>Data will be made publicly available as soon as possible</p>
                        </#if>
                    </div>
                </div>

                <@displayEndUserLicence/>
                <@displayOwnerOfIpr/>
                <@displayData "Use constraints" useConstraints/>
            </div>


            <#if otherInfo?? || supersededData?? || relatedDataHoldings?? || funding?has_content>
                <div class="section section--9">
                    <h2>Miscellaneous</h2>
                    <@displayData "Superseding dataset" supersededData/>
                    <@displayData "Related data" relatedDataHoldings/>
                    <@displayData "Other information" otherInfo/>
                    <@displayGrants/>
                </div>
            </#if>

            <#if description?has_content || lineage?has_content || allKeywords?has_content || topicCategories?? ||  inspireTheme?? || boundingBoxes??>
                <div class="section section--metadata">
                    <h2>Discovery metadata</h2>
                    <p><i>Information in this section will be used to create a record in the EIDC data catalogue.</i></p>
                    <@displayData "Description" description/>
                    <@displayData "Lineage" lineage/>
                    <@displayKeywords/>
                    <@displayArea/>
                </div>
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
            <table>
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
                        <td><#if author.individualName?has_content >${author.individualName}<#else><span class="alert alert--error">missing</span></#if></td>
                        <td><#if author.organisationName?has_content >${author.organisationName}<#else><span class="alert alert--error">missing</span></#if></td>
                        <td><#if author.email?has_content>${author.email}<#else><span class="alert alert--error">missing</span></#if></td>
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
    <p>Files to be deposited</p>
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
                    <td><#if file.name?has_content>${file.name}<#else><span class="alert alert--error">missing</span></#if></td>
                    <td><#if file.format?has_content>${file.format}<#else><span class="alert alert--error">missing</span></#if></td>
                    <td><#if file.size?has_content>${file.size}<#else><span class="alert alert--error">missing</span></#if></td>
                </tr>
            </#items>
            </tbody>
        </table>
    </div>
    </#list>
</#macro>

<#macro displaySupportingDocuments>
    <div class="keyvalue">
        <div class="key">Supporting documents</div>
        <div class="value">
            <#list supportingDocumentNames as name>
                ${name}<#sep><br></#sep>
            </#list>
        </div>
    </div>
</#macro>

<#macro displayEndUserLicence>
    <div class="keyvalue">
        <div class="key">Licence</div>
        <div class="value">
            <#if endUserLicence?has_content && endUserLicence.code == 'license'>
                <#assign licenceText = endUserLicence.value?replace('is available','will be made available') >
                <#if endUserLicence.uri?has_content>
                    <a rel="license" href="${endUserLicence.uri}">${licenceText}</a>
                <#else>
                    ${licenceText}
                </#if>
            </#if>
        </div>
    </div>
</#macro>

<#macro displayOwnerOfIpr>
    <#if ownersOfIpr?has_content && ownersOfIpr?size gt 0 >
     <div class="keyvalue">
        <div class="key">Owner<#if ownersOfIpr?size gt 1>s</#if> of IPR</div>
        <div class="value">
            <#list ownersOfIpr>
                <#items as owner>
                    <div class="responsibleParty">
                        <@func.displayContact owner false true true />
                    </div>
                </#items>
            </#list>
        </div>
    </div>
    </#if>
</#macro>

<#macro displayGrants>
    <#if funding?has_content && funding?size gt 0 >
     <div class="keyvalue">
        <div class="key">Funding</div>
        <div class="value">
            <#list funding>
                <#items as grant>
                   <div>${grant.awardNumber} (${grant.funderName})</div>
                </#items>
            </#list>
        </div>
    </div>
    </#if>
</#macro>


<#macro displayKeywords>
    <dl class="keyvalue">
      <#include "../document/_tags.ftlh">
    </dl>
</#macro>

<#macro displayArea>
    <#if boundingBoxes??>
        <div class="keyvalue">
            <div class="key">Area of study</div>
            <div class="key">
                <div id="studyarea-map">
                    <#list boundingBoxes as extent>
                        <span content="${extent.bounds}" datatype="geoJson"/>
                    </#list>
                </div>
            </div>
        </div>
    </#if>
</#macro>
