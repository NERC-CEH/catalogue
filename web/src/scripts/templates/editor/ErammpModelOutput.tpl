<div class="row">
  <div class="col-sm-3">
    <label for="<%= data.modelAttribute %><%= data.index %>InternalName">Internal name</label>
  </div>
  <div class="col-sm-9">
    <input data-name="internalName" id="<%= data.modelAttribute %><%= data.index %>InternalName" class="editor-input" value="<%= data.internalName %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-3">
    <label for="<%= data.modelAttribute %><%= data.index %>Definition">Definition</label>
  </div>
  <div class="col-sm-9">
    <input data-name="definition" id="<%= data.modelAttribute %><%= data.index %>Definition" class="editor-input" value="<%= data.definition %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-3">
    <label for="<%= data.datatype %><%= data.index %>Datatype">Data type</label>
  </div>
  <div class="col-sm-9">
    <input data-name="datatype" id="<%= data.modelAttribute %><%= data.index %>Datatype" class="editor-input" value="<%= data.datatype %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-3">
    <label for="<%= data.modelAttribute %><%= data.index %>Unit">Unit</label>
  </div>
  <div class="col-sm-9">
    <input data-name="unit" id="<%= data.modelAttribute %><%= data.index %>Unit" class="editor-input" value="<%= data.unit %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-3">
    <label for="<%= data.modelAttribute %><%= data.index %>SpatialResolution">Spatial resolution</label>
  </div>
  <div class="col-sm-9">
    <input data-name="spatialResolution" list="spatialResolution" id="<%= data.modelAttribute %><%= data.index %>SpatialResolution" class="editor-input" value="<%= data.spatialResolution %>">
  </div>
</div><div class="row">
  <div class="col-sm-3">
    <label for="<%= data.modelAttribute %><%= data.index %>TemporalResolution">Temporal resolution</label>
  </div>
  <div class="col-sm-9">
    <input data-name="temporalResolution" list="temporalResolution" id="<%= data.modelAttribute %><%= data.index %>TemporalResolution" class="editor-input" value="<%= data.temporalResolution %>" >
  </div>
</div>
<div class="row">
  <div class="col-sm-3">
    <label for="<%= data.modelAttribute %><%= data.index %>Reuse">Reuse</label>
  </div>
  <div class="col-sm-9">
    <input data-name="reuse" id="<%= data.modelAttribute %><%= data.index %>Reuse" class="editor-input" value="<%= data.reuse %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-3">
    <label for="<%= data.modelAttribute %><%= data.index %>Displayed">Displayed</label>
  </div>
  <div class="col-sm-9">
    <input data-name="displayed" id="<%= data.modelAttribute %><%= data.index %>Displayed" class="editor-input" value="<%= data.displayed %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-3">
    <label for="<%= data.modelAttribute %><%= data.index %>DisplayFormat">DisplayFormat</label>
  </div>
  <div class="col-sm-9">
    <input data-name="displayFormat" list="displayFormat" id="<%= data.modelAttribute %><%= data.index %>DisplayFormat" class="editor-input" value="<%= data.displayFormat %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-3">
    <label for="<%= data.modelAttribute %><%= data.index %>Ipr">IPR</label>
  </div>
  <div class="col-sm-9">
    <input data-name="ipr" id="<%= data.modelAttribute %><%= data.index %>Ipr" class="editor-input" value="<%= data.ipr %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-3">
    <label for="<%= data.modelAttribute %><%= data.index %>AdditionalNotes">Notes</label>
  </div>
  <div class="col-sm-9">
    <input data-name="additionalNotes" id="<%= data.modelAttribute %><%= data.index %>AdditionalNotes" class="editor-input" value="<%= data.additionalNotes %>">
  </div>
</div>

<datalist id="spatialResolution">
  <option value="Grid">
  <option value="Polygon">
  <option value="Cluster">
  <option value="River basin">
  <option value="Catchment">
  <option value="NUTS2">
  <option value="Country">
  <option value="Global">
</datalist>
<datalist id="temporalResolution">
  <option value="Sub-daily">
  <option value="Daily">
  <option value="Monthly">
  <option value="Seasonal">
  <option value="Annual">
</datalist>
<datalist id="displayFormat">
  <option value="Map">
  <option value="Graph">
  <option value="Bar chart">
  <option value="Smiley">
</datalist>