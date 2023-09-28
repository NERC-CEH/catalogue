import _ from 'underscore'

export default _.template(`
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="observedCapabilities<%= data.index %>ObservingTime">Observing Time</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name="observingTime" id="observedCapabilities<%= data.index %>ObservingTime" class="editor-input" value="<%= data.observingTime %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-12 col-lg-12">
    <label>Observed Property</label>
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="observedCapabilities<%= data.index %>NameTitle">Name: title</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name="observedPropertyName.title" id="observedCapabilities<%= data.index %>NameTitle" class="editor-input" value="<%= data.observedPropertyName.title %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="observedCapabilities<%= data.index %>NameHref">Name: url</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name="observedPropertyName.href" id="observedCapabilities<%= data.index %>NameHref" class="editor-input" value="<%= data.observedPropertyName.href %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="observedCapabilities<%= data.index %>UnitOfMeasureTitle">Unit of Measure: title</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name="observedPropertyUnitOfMeasure.title" id="observedCapabilities<%= data.index %>UnitOfMeasureTitle" class="editor-input" value="<%= data.observedPropertyUnitOfMeasure.title %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="observedCapabilities<%= data.index %>UnitOfMeasureHref">Unit of Measure: url</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name="observedPropertyUnitOfMeasure.href" id="observedCapabilities<%= data.index %>UnitOfMeasureHref" class="editor-input" value="<%= data.observedPropertyUnitOfMeasure.href %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-12 col-lg-12">
    <label>Procedure</label>
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="observedCapabilities<%= data.index %>ProcedureNameTitle">Name: title</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name="procedureName.title" id="observedCapabilities<%= data.index %>ProcedureNameTitle" class="editor-input" value="<%= data.procedureName.title %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="observedCapabilities<%= data.index %>ProcedureNameHref">Name: url</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name="procedureName.href" id="observedCapabilities<%= data.index %>ProcedureNameHref" class="editor-input" value="<%= data.procedureName.href %>">
  </div>
</div>
`)
