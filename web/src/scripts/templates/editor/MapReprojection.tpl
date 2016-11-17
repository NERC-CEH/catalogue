<div class="row">
  <div class="col-sm-offset-3 col-lg-offset-3 col-sm-2 col-lg-2">
    <label class="control-label" for="mapReprojectionSource<%= data.index %>EpsgCode">Reprojected EPSG:</label>
  </div>
  <div class="col-sm-2 col-lg-2">
    <input data-name='epsgCode' class="editor-input" id="mapReprojectionSource<%= data.index %>EpsgCode" value="<%= data.epsgCode %>">
  </div>
  <div class="col-sm-offset-1 col-lg-offset-1 col-sm-4 col-lg-4">
    <input data-name='path' class="editor-input" id="mapReprojectionSource<%= data.index %>Path" value="<%= data.path %>">
  </div>
</div>