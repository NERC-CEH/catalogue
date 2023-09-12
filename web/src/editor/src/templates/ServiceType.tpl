<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label for="serviceType">Type</label>
  </div>
  <div class="col-sm-11 col-lg-11">
    <select data-name="type" class="editor-input" id="serviceType" <%= data.disabled%>>
      <option value="">- Select Service Type -</option>
      <optgroup label="INSPIRE">
        <option value="discovery">Discovery Service</option>
        <option value="download">Download Service</option>
        <option value="invoke">Invoke Service</option>
        <option value="other">Other Service</option>
        <option value="transformation">Transformation Service</option>
        <option value="view">View Service</option>
      </optgroup>
      <optgroup label="OGC">
        <option value="WFS-G">Gazetteer Service</option>
        <option value="SOS">Sensor Observation Service</option>
        <option value="SPS">Sensor Processing Service</option>
        <option value="CSW">Web Catalog Service</option>
        <option value="WCS">Web Coverage Service</option>
        <option value="WFS">Web Feature Service</option>
        <option value="WFS-T">Web Feature Service (transactional)</option>
        <option value="WMS">Web Map Service</option>
        <option value="WMTS">Web Map Tile Service</option>
        <option value="WNS">Web Notification Service</option>
        <option value="WPOS">Web Price &amp; Ordering Service</option>
        <option value="WPS">Web Processing Service</option>
        <option value="WTS">Web Terrain Service</option>
      </optgroup>
    </select>
  </div>
</div>