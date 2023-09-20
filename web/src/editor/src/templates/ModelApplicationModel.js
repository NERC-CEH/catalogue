import _ from 'underscore'

export default _.template(`
<div class="row">
  <div class="col-sm-1">
    <label for="modelApplicationModel<%= data.index %>Name">Name</label>
  </div>
  <div class="col-sm-5">
    <input data-name="name" id="modelApplicationModel<%= data.index %>Name" class="editor-input" value="<%= data.name %>">
  </div>
  <div class="col-sm-1">
    <label for="modelApplicationModel<%= data.index %>Version">Version</label>
  </div>
  <div class="col-sm-5">
    <input data-name="version" id="modelApplicationModel<%= data.index %>Version" class="editor-input" value="<%= data.version %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-1">
    <label for="modelApplicationModel<%= data.index %>PrimaryPurpose">Primary Purpose</label>
  </div>
  <div class="col-sm-5">
    <input data-name="primaryPurpose" id="modelApplicationModel<%= data.index %>PrimaryPurpose" class="editor-input" value="<%= data.primaryPurpose %>">
  </div>
  <div class="col-sm-1">
    <label for="modelApplicationModel<%= data.index %>ApplicationScale">Application Scale</label>
  </div>
  <div class="col-sm-5">
    <select data-name="applicationScale" id="modelApplicationModel<%= data.index %>ApplicationScale" class="editor-input">
      <option value="plot">plot</option>
      <option value="field">field</option>
      <option value="farm">farm</option>
      <option value="river reach">river reach</option>
      <option value="catchment">catchment</option>
      <option value="landscape">landscape</option>
      <option value="regional">regional</option>
      <option value="national">national</option>
    </select>
  </div>
</div>
<div class="row">
  <div class="col-sm-1">
    <label for="modelApplicationModel<%= data.index %>KeyOutputVariables">Key Output Variables</label>
  </div>
  <div class="col-sm-5">
    <input data-name="keyOutputVariables" id="modelApplicationModel<%= data.index %>KeyOutputVariables" class="editor-input" value="<%= data.keyOutputVariables %>">
  </div>
  <div class="col-sm-1">
    <label for="modelApplicationModel<%= data.index %>KeyInputVariables">Key Input Variables</label>
  </div>
  <div class="col-sm-5">
    <input data-name="keyInputVariables" id="modelApplicationModel<%= data.index %>KeyInputVariables" class="editor-input" value="<%= data.keyInputVariables %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-1">
    <label for="modelApplicationModel<%= data.index %>TemporalResolution">Temporal Resolution</label>
  </div>
  <div class="col-sm-5">
    <input data-name="temporalResolution" id="modelApplicationModel<%= data.index %>TemporalResolution" class="editor-input" value="<%= data.temporalResolution %>">
  </div>
  <div class="col-sm-1">
    <label for="modelApplicationModel<%= data.index %>SpatialResolution">Spatial Resolution</label>
  </div>
  <div class="col-sm-5">
    <input data-name="spatialResolution" id="modelApplicationModel<%= data.index %>SpatialResolution" class="editor-input" value="<%= data.spatialResolution %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-1">
    <label for="modelApplicationModel<%= data.index %>InputDataAvailableInDataCatalogue">Input Data Available In Data Catalogue</label>
  </div>
  <div class="col-sm-5">
    <input data-name="inputDataAvailableInDataCatalogue" id="modelApplicationModel<%= data.index %>InputDataAvailableInDataCatalogue" class="editor-input" value="<%= data.inputDataAvailableInDataCatalogue %>">
  </div>
</div>
`)
