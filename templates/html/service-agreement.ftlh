<#import "blocks.ftlh" as blocks>
<#import "skeleton.ftlh" as skeleton>
<#import "../functions.ftlh" as func>
<#if ownerOfIpr?has_content>
    <#assign
      authors = func.filter(ownerOfIpr, "role", "author")
      pocs = func.filter(ownerOfIpr, "role", "pointOfContact")
      custodians = func.filter(ownerOfIpr, "role", "custodian")
      publishers = func.filter(ownerOfIpr, "role", "publisher")
      depositors = func.filter(ownerOfIpr, "role", "depositor")
      originators = func.filter(ownerOfIpr, "role", "originator")
      rightsHolders = func.filter(ownerOfIpr, "role", "rightsHolder")
      owners = func.filter(ownerOfIpr, "role", "owner")
      originators = func.filter(ownerOfIpr, "role", "originator")
      resourceProviders = func.filter(ownerOfIpr, "role", "resourceProvider")
      otherContacts = custodians + publishers + depositors + originators + owners + resourceProviders + rightsHolders
/>
</#if>
<#if endUserLicence?has_content>
    <#assign
    licences = func.filter(endUserLicence, "code", "license")
    otherConstraints =  func.filter(endUserLicence, "code", "license", true)>
</#if>

<#assign
rel_all=jena.allRelatedRecords(uri)
rel_related=jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#relatedTo") + jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#relatedTo")
rel_generates = jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#generates")
rel_uses = jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#uses")
rel_memberOf = jena.relationships(uri, "https://vocabs.ceh.ac.uk/eidc#memberOf")
rel_generatedBy= jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#generates")
rel_supersededBy = jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#supersedes")
rel_usedBy = jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#uses")
rel_hasMember = jena.inverseRelationships(uri, "https://vocabs.ceh.ac.uk/eidc#memberOf")
rel_supersedes = jena.supersedes(uri)
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
        </div>
      </div>
    </#if>

    <h1>Service Agreement</h1>
    <h1><b>* IMPORTANT *	ONCE A DOI HAS BEEN ISSUED,
            THESE DETAILS CANNOT BE CHANGED</b></h1>
    <h1>Environmental Information Data Centre - Service Agreement</h1>
    <p>For more information/guidance about this document see <a href="https://eidc.ac.uk/support/agreement">https://eidc.ac.uk/support/agreement</a> </p>
    <h2>The Agreement</h2>
    <p>As a depositor you must consent to this service agreement which establishes the terms and conditions of use of your data.  This document sets out your rights and responsibilities as depositor and ours as the data distributor.
        This agreement authorises the EIDC to preserve and to distribute the data under the terms specified in this document.  To do so, the EIDC will store the data and, where necessary, duplicate and transform copies.</p>
    <h3>Our responsibilities:</h3>
    <ul>
        <li>We will take every care to preserve the integrity of the data and protect it from loss or damage</li>
        <li>We will make the data publicly available, subject to any conditions specified in this Service Agreement.</li>
        <li>Unless otherwise requested, we will issue a Digital Object Identifier (DOI) to facilitate the proper citation of the dataset.</li>
        <li>We will ask users of the data to agree to an end user licence.</li>
        <li>We require users of the data to acknowledge and cite sources when re-using data.</li>
        <li>We will only process personal information in connection with long-term archiving services, in accordance with our <a href="https://eidc.ac.uk/policies/privacy">Privacy Notice</a> </li>
    </ul>
    <h3>Your responsibilities:</h3>
    <ul>
        <li>You provide assurances that you are entitled to deposit the data resource in the EIDC and that the agreement of all parties who may have an interest in the resource has been obtained.</li>
        <li>You confirm that the resource is not already available elsewhere.</li>
        <li>You agree to supply sufficient supporting information (metadata) to enable discovery, management and reuse of the data.</li>
        <li>You ensure that in the case of research data with human subjects that consents collected are ethically and legally appropriate and sufficient to allow deposit of the resource.</li>
        <li>You agree to notify the EIDC promptly of any copyright, confidentiality, privacy, data protection, defamation or similar issues pertaining to the data.</li>
        <li>You guarantee that nothing in the resource or supporting information contravenes the UK data protection laws or any other UK law (e.g., the Wildlife and Countryside Act 1981).</li>
        <li>You agree to notify the EIDC if it is subsequently found that the data resource is no longer fit for purpose (e.g. errors are found in the dataset).</li>
    </ul>
    <#if depositReference??> <p>Deposit reference: ${depositReference}</p> </#if>
    <#if depositorName??> <p>Depositor name: ${depositorName}</p> </#if>
    <#if depositorContactDetails??> <p>Depositor contact details: ${depositorContactDetails}</p> </#if>
    <#if eidcName??> <p>For the EIDC name: ${eidcName}</p> </#if>
    <#if eidcContactDetails??> <p>For the EIDC contact details: ${eidcContactDetails}</p> </#if>
    <h2>ONE: Data identification and citation</h2>
    <#include "document/_authors.ftlh">
    <h2>TWO: Policies & legislation</h2>
    <#if otherPoliciesOrLegislation??> <p>Other Policies or Legislation : ${otherPoliciesOrLegislation}</p> </#if>
    <h2>THREE: The data</h2>
    <#include "document/_files.ftlh">
    <#if transferMethod??> <p>Transfer method: ${transferMethod}</p> </#if>
    <#if relatedRecords??> <#include "document/_related.ftlh"> </#if> <#-- to be replaced with related records -->
    <#if topicCategories??> <#include "document/_tags.ftlh"> </#if>
    <h2>FOUR: Supporting documentation</h2>
    <#if supportingDocumentNames??> <#include "document/_supportingDocuments.ftlh"> </#if>
    <#if contentIncluded??> <p>Content Included: ${contentIncluded}</p> </#if>
    <h2>FIVE: Data retention</h2>
    <#if policyExceptions??> <p>Policy Exceptions: ${policyExceptions}</p> </#if>
    <h2>SIX: Availability and access</h2>
    <#if availability??> <p>Availability: ${availability}</p> </#if>
    <#if specificRequirements??> <p>Specific Requirements: ${specificRequirements}</p> </#if>
    <#if otherServicesRequired??> <p>Other services required: ${otherServicesRequired}</p> </#if>
    <h2>SEVEN: Licensing and IPR</h2>
    <#if licences??> <#include "document/_serviceAgreementLicence.ftlh"> </#if>
    <#if ownerOfIpr??> <#include "document/_contacts.ftlh"> </#if>
    <#if useConstraints??> <p>Use constraints: ${useConstraints}</p> </#if>
    <h2>EIGHT: Superseding existing data (if applicable)</h2>
    <#if supersededMetadataId??> <p>Metadata Id: ${supersededMetadataId}</p> </#if>
    <#if supersededReason??> <p>Reason: ${supersededReason}</p> </#if>
    <h2>NINE: Miscellaneous</h2>
    <#if otherInfo??> <p>Other Info: ${otherInfo}</p> </#if>
    <h2>TEN: Discovery metadata</h2>
    <#if description??> <p>Description: ${description}</p> </#if>
    <#if lineage??> <p>Lineage: ${lineage}</p> </#if>
    <#if keywords??><#include "document/_keywords.ftlh">  </#if>
    <#if areaOfStudy?has_content>
        <#include "document/_extent.ftlh">
    </#if>
  </div>
</div>
</@skeleton.master>