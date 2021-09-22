<#import "skeleton.ftlh" as skeleton>

<@skeleton.master "Service Agreement">
    <h1>Service Agreement</h1>
    <h2>${title}</h2>
    <p>Deposit reference: ${depositReference}</p>
    <p>Depositor name: ${depositor.name}</p>
    <p>Depositor contact details: ${depositor.contactDetails}</p>
    <p>For the EIDC name: ${forTheEIDC.name}</p>
    <p>For the EIDC contact details: ${forTheEIDC.contactDetails}</p>
    <p>DOI: ${DOI}</p>
    <h2>ONE: Data identification and citation</h2>
    <p>Author name: ${author.name}</p>
    <p>Author affiliation: ${author.affiliation}</p>
    <p>Author email address: ${author.emailAddress}</p>
    <p>Author ORCID: ${author.ORCID}</p>
    <h2>TWO: Policies & legislation</h2>
    <h2>THREE: The data</h2>
    <p>Data files: ${dataFiles}</p>
    <p>File names: ${fileNames}</p>
    <p>File formats: ${fileFormats}</p>
    <p>File sizes: ${fileSize}</p>
    <p>Transfer method: ${transferMethod}</p>
    <p>Related records: ${relatedRecords}</p>
    <p>Data category: ${dataCategory}</p>
    <p>Documents: ${documents}</p>
    <h2>FOUR: Supporting documentation</h2>
    <h2>FIVE: Data retention</h2>
    <h2>SIX: Availability and access</h2>
    <p>Availability: ${availability}</p>
    <p>Specific Requirements: ${specificRequirements}</p>
    <p>Other services required: ${otherServicesRequired}</p>
    <h2>SEVEN: Licensing and IPR<h2>>
    <p>End user license: ${endUserLicense}</p>
    <p>Owner of IPR: ${ownerOfIpr}</p>
    <p>User constraints: ${userConstraints}</p>
    <h2>EIGHT: Superseding existing data (if applicable)</h2>
    <p>Super seeded metadata Id: ${superSeededMetadataId}</p>
    <p>${reason}</p>
    <h2>NINE: Miscellaneous</h2>
    <h2>TEN: Discovery metadata</h2>
    <p>Description: ${description}</p>
    <p>Lineage: ${lineage}</p>
    <p>Keywords: ${Keywords}</p>
    <p>Area of study: ${areaOfStudy}</p>
    <p>This is a service agreement</p>
</@skeleton.master>