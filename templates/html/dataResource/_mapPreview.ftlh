<#if wmsMapList?has_content || (dataPreviewOffered!false)>
  <div id="segment-map">
    <h2>Data Preview</h2>
    <#if wmsMapList?has_content>
    <#assign wmsPrimary = wmsMapList?first>
    <div class="wms-map-preview">
      <#list boundingBoxes![] as extent>
        <span class="wms-extent" data-west="${extent.westBoundLongitude?c}" data-east="${extent.eastBoundLongitude?c}" data-south="${extent.southBoundLatitude?c}" data-north="${extent.northBoundLatitude?c}"></span>
      </#list>
      <#list wmsMapList as m>
        <div class="wms-service" data-title="${m.title!}" data-local-path="${m.localPath}" data-public-url="${m.publicMapUrl}" data-capabilities-url="${m.capabilitiesUrl}">
          <#list m.layerNames as layerName>
            <span class="wms-layer-name">${layerName}</span>
          </#list>
          <#list m.legends as layerName, entries>
            <span class="wms-legend-layer" data-layer="${layerName}">
              <#list entries as entry>
                <span class="wms-legend-class" data-colour="${entry.colour!}" data-shape="${entry.shape}">${entry.label}</span>
              </#list>
            </span>
          </#list>
        </div>
      </#list>
      <div class="wms-map-unavailable d-none">Map preview unavailable.</div>
      <div class="wms-map"></div>
      <div class="wms-layers"></div>
      <#-- Collapsed until asked for; the summary's disclosure marker shows it can be opened -->
      <details class="wms-gis-help">
        <summary><h3 class="h5">Use this map in your GIS</h3></summary>
        <p>This map is a Web Map Service (WMS). In QGIS choose <em>Layer ▸ Add Layer ▸ Add WMS/WMTS Layer…</em>, click <em>New</em>, paste the GetCapabilities address below into <em>URL</em>, then <em>Connect</em> and add the layer <strong class="wms-layer-label"></strong>. Other GIS tools and web maps can use the same address.</p>
        <div class="mb-3">
          <label class="form-label" for="wms-capabilities-url">GetCapabilities</label>
          <div class="input-group">
            <input id="wms-capabilities-url" type="text" readonly class="form-control font-monospace wms-capabilities-url" value="${wmsPrimary.capabilitiesUrl}">
            <button type="button" class="btn btn-outline-secondary">Copy</button>
          </div>
        </div>
        <div class="mb-3">
          <label class="form-label wms-getmap-label" for="wms-getmap-url">GetMap for this view (British National Grid, EPSG:27700)</label>
          <div class="input-group">
            <input id="wms-getmap-url" type="text" readonly class="form-control font-monospace wms-getmap-url">
            <button type="button" class="btn btn-outline-secondary">Copy</button>
          </div>
        </div>
      </details>
    </div>
    </#if>
    <#if dataPreviewOffered!false>
      <#assign previewAccess = downloadOrderDetails.from(onlineResources![])>
      <#assign previewPackage = previewAccess.dataAccessResources?filter(r -> r.function == "fileAccess" || r.function == "download")>
      <div class="data-preview" data-record-id="${id}" data-package-url="<#if previewPackage?has_content>/download/${id}?url=${previewPackage?first.url}</#if>" data-supporting-documents-url="${previewAccess.supportingDocumentsUrl!}">
        <div class="row g-3">
          <div class="col-lg-7 data-preview-data"></div>
          <div class="col-lg-5 data-preview-documents"></div>
        </div>
      </div>
    </#if>
  </div>
</#if>
