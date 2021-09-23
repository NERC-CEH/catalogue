<#import "skeleton.ftlh" as skeleton>

<@skeleton.master "Service Agreement">
    <h1>Service Agreement</h1>
    <h2>${title}</h2>
    <#if depositReference??> <p>Deposit reference: ${depositReference}</p> </#if>
    <#if (depositor.name)??> <p>Depositor name: ${depositor.name}</p> </#if>
    <#if (depositor.contactDetails)??> <p>Depositor contact details: ${depositor.contactDetails}</p> </#if>
    <#if (forTheEIDC.name)??> <p>For the EIDC name: ${forTheEIDC.name}</p> </#if>
    <#if (forTheEIDC.contactDetails)??> <p>For the EIDC contact details: ${forTheEIDC.contactDetails}</p> </#if>
    <#if DOI??> <p>DOI: ${DOI}</p> </#if>
    <h2>ONE: Data identification and citation</h2>
    <#if (author.name)??> <p>Author name: ${author.name}</p> </#if>
    <#if (author.affiliation)??> <p>Author affiliation: ${author.affiliation}</p> </#if>
    <#if (author.emailAddress)??> <p>Author email address: ${author.emailAddress}</p> </#if>
    <#if (author.ORCID)??> <p>Author ORCID: ${author.ORCID}</p> </#if>
    <h2>TWO: Policies & legislation</h2>
    <h2>THREE: The data</h2>
    <#if dataFiles??> <p>Data files: ${dataFiles}</p> </#if>
    <#if fileNames??> <p>File names: ${fileNames}</p> </#if>
    <#if fileFormats??> <p>File formats: ${fileFormats}</p> </#if>
    <#if fileSize??> <p>File sizes: ${fileSize}</p> </#if>
    <#if transferMethod??> <p>Transfer method: ${transferMethod}</p> </#if>
    <#if relatedRecords??> <p>Related records: ${relatedRecords}</p> </#if>
    <#if dataCategory??> <p>Data category: ${dataCategory}</p> </#if>
    <#if documents??> <p>Documents: ${documents}</p> </#if>
    <h2>FOUR: Supporting documentation</h2>
    <h2>FIVE: Data retention</h2>
    <h2>SIX: Availability and access</h2>
    <#if availability??> <p>Availability: ${availability}</p> </#if>
    <#if specificRequirements??> <p>Specific Requirements: ${specificRequirements}</p> </#if>
    <#if otherServicesRequired??> <p>Other services required: ${otherServicesRequired}</p> </#if>
    <h2>SEVEN: Licensing and IPR<h2>
    <#if endUserLicense??> <p>End user license: ${endUserLicense}</p> </#if>
    <#if ownerOfIpr??> <p>Owner of IPR: ${ownerOfIpr}</p> </#if>
    <#if userConstraints??> <p>User constraints: ${userConstraints}</p> </#if>
    <h2>EIGHT: Superseding existing data (if applicable)</h2>
    <#if superSeededMetadataId??> <p>Super seeded metadata Id: ${superSeededMetadataId}</p> </#if>
    <#if reason??> <p>${reason}</p> </#if>
    <h2>NINE: Miscellaneous</h2>
    <h2>TEN: Discovery metadata</h2>
    <#if description??> <p>Description: ${description}</p> </#if>
    <#if lineage??> <p>Lineage: ${lineage}</p> </#if>
    <#if Keywords??> <p>Keywords: ${Keywords}</p> </#if>
    <#if areaOfStudy??> <p>Area of study: ${areaOfStudy}</p> </#if>
    <p>This is a service agreement</p>
</@skeleton.master>