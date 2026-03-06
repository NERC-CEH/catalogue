<div class="col-xl-3 col-lg-4 col-md-5 d-none d-md-block py-3 tableOfContents" id="tableOfContents">
    <div class="list-group list-group-flush sticky-top">
      <a class="list-group-item list-group-item-action text-body-tertiary" href="#top"><i class="fa-solid fa-angle-up fa-xs"></i> Top</a>
      <#if description?? && description?has_content><a class="list-group-item list-group-item-action" href="#segment-description">Description</a></#if>
      <#if distributionFormats?? && distributionFormats?has_content><a class="list-group-item list-group-item-action" href="#segment-formats">Format</a></#if>
      <#if boundingBoxes?has_content || spatialRepresentationTypes?? || spatialReferenceSystems??><a class="list-group-item list-group-item-action" href="#segment-spatial">Spatial information</a></#if>
      <#if temporalExtents?? || temporalResolution?? ><a class="list-group-item list-group-item-action" href="#segment-temporal">Temporal information</a></#if>
      <#if lineage?? && lineage?has_content><a class="list-group-item list-group-item-action" href="#segment-quality">Provenance &amp; quality</a></#if>
      <#if lineage?? && lineage?has_content><a class="list-group-item list-group-item-action" href="#segment-constraints">Licensing &amp; constraints</a></#if>
      <#if (rel_partOf?has_content && rel_partOf?size gt 0) || (rel_usedBy?has_content && rel_usedBy?size gt 0) || (rel_uses?has_content && rel_uses?size gt 0) || (rel_generatedBy?has_content && rel_generatedBy?size gt 0) || (rel_generates?has_content && rel_generates?size gt 0) || (rel_related?has_content && rel_related?size gt 0) ><a class="list-group-item list-group-item-action" href="#segment-associations">Related records</a></#if>
      <#if incomingCitations?has_content><a class="list-group-item list-group-item-action" href="#segment-citations">Citations</a></#if>
      <#if supplemental?has_content><a class="list-group-item list-group-item-action" href="#segment-supplemental">Supplemental information</a></#if>
      <#if pointsOfContact?size gt 0><a class="list-group-item list-group-item-action" href="#segment-pointOfContact">Correspondence</a></#if>
      <#if authors?size gt 0><a class="list-group-item list-group-item-action" href="#segment-authors">Authors</a></#if>
      <a class="list-group-item list-group-item-action" href="#segment-additional">Additional metadata</a>
      <#if funding?has_content><a class="list-group-item list-group-item-action" href="#segment-funding">Funding</a></#if>
      <a class="list-group-item list-group-item-action" data-bs-toggle="offcanvas" href="#access" role="button" aria-controls="access">Download/Access</a>
      <#if permission.userCanEdit(id) && metadata.catalogue == "eidc" && totalIssues?? && totalIssues gte 1><a class="list-group-item list-group-item-action link-${metadataClass}" href="#segment-metadataQuality"><span class="badge rounded-pill bg-${metadataClass}" title="Metadata issues">${totalIssues}</span> Metadata issues</a></#if>
    </div>
</div>





