<#if wmsMapList?has_content>
  <#assign wmsPrimary = wmsMapList?first>
  <div id="segment-map">
    <h2>Map</h2>
    <div class="wms-map-preview">
      <#list boundingBoxes![] as extent>
        <span class="wms-extent" data-west="${extent.westBoundLongitude?c}" data-east="${extent.eastBoundLongitude?c}" data-south="${extent.southBoundLatitude?c}" data-north="${extent.northBoundLatitude?c}"></span>
      </#list>
      <#list wmsMapList as m>
        <div class="wms-service" data-title="${m.title!}" data-local-path="${m.localPath}" data-public-url="${m.publicMapUrl}" data-capabilities-url="${m.capabilitiesUrl}">
          <#list m.layerNames as layerName>
            <span class="wms-layer-name">${layerName}</span>
          </#list>
        </div>
      </#list>
      <div class="wms-map-unavailable d-none">Map preview unavailable.</div>
      <div class="wms-map"></div>
      <div class="wms-layers"></div>
      <div class="wms-gis-help">
        <h3 class="h5">Use this map in your GIS</h3>
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
      </div>
    </div>
  </div>
</#if>
