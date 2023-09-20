import _ from 'underscore'

export default _.template(`
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="dataInfo<%= data.index %>Name">Parameter name</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name="name" id="dataInfo<%= data.index %>Name" class="editor-input" value="<%= data.name %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="dataInfo<%= data.index %>Units">Units</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name="units" id="dataInfo<%= data.index %>Units" class="editor-input" value="<%= data.units %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="dataInfo<%= data.index %>FileFormat">File format</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name="fileFormat" id="dataInfo<%= data.index %>FileFormat" class="editor-input" value="<%= data.fileFormat %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="dataInfo<%= data.index %>Url">Url</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name="url" id="dataInfo<%= data.index %>Url" class="editor-input" value="<%= data.url %>">
  </div>
</div>
`)
