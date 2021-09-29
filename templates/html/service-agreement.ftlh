<#import "skeleton.ftlh" as skeleton>

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
    <h2>${title}</h2>
    <#if depositReference??> <p>Deposit reference: ${depositReference}</p> </#if>
    <#if depositorName??> <p>Depositor name: ${depositorName}</p> </#if>
    <#if depositorContactDetails??> <p>Depositor contact details: ${depositorContactDetails}</p> </#if>
    <#if eidcName??> <p>For the EIDC name: ${eidcName}</p> </#if>
    <#if eidcContactDetails??> <p>For the EIDC contact details: ${eidcContactDetails}</p> </#if>
    <h2>ONE: Data identification and citation</h2>
    <#if authorsName??> <p>Author name: ${authorsName}</p> </#if>
    <#if affiliation??> <p>Author affiliation: ${affiliation}</p> </#if>
    <#if emailAddress??> <p>Author email address: ${emailAddress}</p> </#if>
    <#if orcidId??> <p>Author ORCID: ${orcidId}</p> </#if>
    <h2>TWO: Policies & legislation</h2>
    <#if otherPoliciesOrLegislation??> <p>Other Policies or Legislation : ${otherPoliciesOrLegislation}</p> </#if>
    <h2>THREE: The data</h2>
    <#if dataFiles??> <p>Data files: ${dataFiles}</p> </#if>
    <#if fileNames??> <p>File names: ${fileNames}</p> </#if>
    <#if fileFormats??> <p>File formats: ${fileFormats}</p> </#if>
    <#if fileSize??> <p>File sizes: ${fileSize}</p> </#if>
    <#if transferMethod??> <p>Transfer method: ${transferMethod}</p> </#if>
    <#if relatedRecords??> <p>Related Data Holdings: ${relatedRecords}</p> </#if>
    <#if dataCategory??> <p>Data category: ${dataCategory}</p> </#if>
    <h2>FOUR: Supporting documentation</h2>
    <#if supportingDocumentNames??> <p>Supporting Document Names: ${supportingDocumentNames}</p> </#if>
    <#if contentIncluded??> <p>Content Included: ${contentIncluded}</p> </#if>
    <h2>FIVE: Data retention</h2>
    <#if policyExceptions??> <p>Policy Exceptions: ${policyExceptions}</p> </#if>
    <h2>SIX: Availability and access</h2>
    <#if availability??> <p>Availability: ${availability}</p> </#if>
    <#if specificRequirements??> <p>Specific Requirements: ${specificRequirements}</p> </#if>
    <#if otherServicesRequired??> <p>Other services required: ${otherServicesRequired}</p> </#if>
    <h2>SEVEN: Licensing and IPR</h2>
    <#if endUserLicense??> <p>End user license: ${endUserLicense}</p> </#if>
    <#if ownerOfIpr??> <p>Owner of IPR: ${ownerOfIpr}</p> </#if>
    <#if useConstraints??> <p>Use constraints: ${useConstraints}</p> </#if>
    <h2>EIGHT: Superseding existing data (if applicable)</h2>
    <#if supersededMetadataId??> <p>Metadata Id: ${supersededMetadataId}</p> </#if>
    <#if supersededReason??> <p>Reason: ${supersededReason}</p> </#if>
    <h2>NINE: Miscellaneous</h2>
    <#if otherInfo??> <p>Other Info: ${otherInfo}</p> </#if>
    <h2>TEN: Discovery metadata</h2>
    <#if description??> <p>Description: ${description}</p> </#if>
    <#if lineage??> <p>Lineage: ${lineage}</p> </#if>
    <#if Keywords??> <p>Keywords: ${Keywords}</p> </#if>
    <#if areaOfStudy??> <p>Area of study: ${areaOfStudy}</p> </#if>
</div>
</div>
</@skeleton.master>