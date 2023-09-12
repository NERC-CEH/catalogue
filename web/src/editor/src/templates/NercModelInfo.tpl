<div class="row">
  <div class="col-sm-3 col-lg-3">
    <label for="modelInfo<%= data.index %>Name">Name</label>
  </div>
  <div class="col-sm-9 col-lg-9">
    <div class="input-group">
      <span class="input-group-addon" id="sizing-addon2">Search</span>
      <input data-name="name" id="modelInfo<%= data.index %>Name" class="form-control autocomplete" value="<%= data.name %>">
    </div>
  </div>
</div>
<div class="row">
  <div class="col-sm-3 col-lg-3">
    <label for="modelInfo<%= data.index %>Id">Id</label>
  </div>
  <div class="col-sm-9 col-lg-9">
    <input data-name="id" id="modelInfo<%= data.index %>Id" class="editor-input identifier" value="<%= data.id %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-3 col-lg-3">
    <label for="modelInfo<%= data.index %>Notes">Notes</label>
  </div>
  <div class="col-sm-9 col-lg-9">
    <textarea data-name="notes" rows="7" id="modelInfo<%= data.index %>Notes" class="editor-textarea"><%= data.notes %></textarea>
  </div>
</div>
<div class="row">
  <div class="col-sm-3 col-lg-3">
    <label for="modelInfo<%= data.index %>spatialExtentOfApplication">Spatial extent of application</label>
  </div>
  <div class="col-sm-9 col-lg-9">
    <input list="modelInfo<%= data.index %>SpatialExtentOfApplicationList" data-name="spatialExtentOfApplication" id="modelInfo<%= data.index %>SpatialExtentOfApplication" class="editor-input" value="<%= data.spatialExtentOfApplication %>"/>
    <datalist id="modelInfo<%= data.index %>SpatialExtentOfApplicationList">
      <option value="Plot"/>
      <option value="Field"/>
      <option value="Farm"/>
      <option value="River reach"/>
      <option value="Catchment"/>
      <option value="Landscape"/>
      <option value="Regional"/>
      <option value="National"/>
      <option value="Global"/>
    </datalist>
  </div>
</div>
<div class="row">
  <div class="col-sm-3 col-lg-3">
    <label for="modelInfo<%= data.index %>AvailableSpatialData">Available spatial data</label>
  </div>
  <div class="col-sm-9 col-lg-9">
    <select data-name="availableSpatialData" id="modelInfo<%= data.index %>AvailableSpatialData" class="editor-input spatial-data">
      <option value="unknown">Unknown</option>
      <option value="shapefile">Shapefile</option>
      <option value="bounding box">Bounding box</option>
    </select>
  </div>
</div>
<div class="row">
  <div class="col-sm-3 col-lg-3">
    <label for="modelInfo<%= data.index %>SpatialResolutionOfApplication">Spatial resolution of application</label>
  </div>
  <div class="col-sm-9 col-lg-9">
    <input data-name="spatialResolutionOfApplication" id="modelInfo<%= data.index %>SpatialResolutionOfApplication" class="editor-input" value="<%= data.spatialResolutionOfApplication %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-3 col-lg-3">
    <label for="modelInfo<%= data.index %>TemporalExtentOfApplicationStartDate">Temporal extent of application (start date)</label>
  </div>
  <div class="col-sm-9 col-lg-9">
    <input data-name="temporalExtentOfApplicationStartDate" id="modelInfo<%= data.index %>TemporalExtentOfApplicationStartDate" class="editor-input" value="<%= data.temporalExtentOfApplicationStartDate %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-3 col-lg-3">
    <label for="modelInfo<%= data.index %>TemporalExtentOfApplicationEndDate">Temporal extent of application (end date)</label>
  </div>
  <div class="col-sm-9 col-lg-9">
    <input data-name="temporalExtentOfApplicationEndDate" id="modelInfo<%= data.index %>TemporalExtentOfApplicationEndDate" class="editor-input" value="<%= data.temporalExtentOfApplicationEndDate %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-3 col-lg-3">
    <label for="modelInfo<%= data.index %>TemporalResolutionOfApplication">Temporal resolution of application</label>
  </div>
  <div class="col-sm-9 col-lg-9">
    <input data-name="temporalResolutionOfApplication" id="modelInfo<%= data.index %>TemporalResolutionOfApplication" class="editor-input" value="<%= data.temporalResolutionOfApplication %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-3 col-lg-3">
    <label for="modelInfo<%= data.index %>CalibrationConditions">Calibration</label>
  </div>
  <div class="col-sm-9 col-lg-9">
    <textarea data-name="calibrationConditions" rows="3" id="modelInfo<%= data.index %>CalibrationConditions" class="editor-textarea"><%= data.calibrationConditions %></textarea>
  </div>
</div>