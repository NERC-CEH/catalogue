<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="boundingBox<%= data.index %>WestBoundLongitude">West Bounding Longitude</label>
    <input data-name="westBoundLongitude" id="boundingBox<%= data.index %>WestBoundLongitude" class="editor-input" value="<%= data.westBoundLongitude %>">
  </div>
  <div class="col-sm-2 col-lg-2">
    <label for="boundingBox<%= data.index %>NorthBoundLatitude">North Bounding Latitude</label>
    <input data-name="northBoundLatitude" id="boundingBox<%= data.index %>NorthBoundLatitude" class="editor-input" value="<%= data.northBoundLatitude %>">
    <label for="boundingBox<%= data.index %>SouthBoundLatitude">South Bounding Latitude</label>
    <input data-name="southBoundLatitude" id="boundingBox<%= data.index %>SouthBoundLatitude" class="editor-input" value="<%= data.southBoundLatitude %>">
  </div>
  <div class="col-sm-2 col-lg-2">
    <label for="boundingBox<%= data.index %>EastBoundLongitude">East Bounding Longitude</label>
    <input data-name="eastBoundLongitude" id="boundingBox<%= data.index %>EastBoundLongitude" class="editor-input" value="<%= data.eastBoundLongitude %>">
  </div>
  <% if (data.index === 'Add') { %>
    <div class="col-sm-4 col-lg-4">
      <br><br>
      <select class="editor-input">
        <option value="">- Select Predefined Extent -</option>
        <option value="eng">England</option>
        <option value="gb">Great Britain</option>
        <option value="ni">Northern Ireland</option>
        <option value="sco">Scotland</option>
        <option value="uk">United Kingdom</option>
        <option value="wal">Wales</option>
        <option value="wor">World</option>
      </select>
    </div>
  <% } %>
</div>