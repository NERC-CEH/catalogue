<#compress>

<#if type=='dataset' || type=='nonGeographicDataset' || type=='signpost'>
  <#assign docType = "Dataset">
<#elseif type=='aggregate' || type=='series'>
  <#assign docType = "Series">
<#elseif type=='application'>
  <#assign docType = "SoftwareSourceCode">
</#if>

<#if docType?has_content>
  {
  "@context": "https://w3id.org/ro/crate/1.1/context",
    "@graph": [
      {
        "@type": "CreativeWork",
        "@id": "ro-crate-metadata.json",
        "conformsTo": { "@id": "https://w3id.org/ro/crate/1.1" },
        "about": {
          "@id": "${uri?trim}"
        }
      },
      <#include "../schema.org/schema.org.ftlh">
    ]
  }
</#if>
</#compress>
