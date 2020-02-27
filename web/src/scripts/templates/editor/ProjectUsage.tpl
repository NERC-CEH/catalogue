<div class="row">
   <div class="col-sm-3 col-md-2">
    <label for="projectUsage<%= data.index %>Project">Project name</label>
  </div>
  <div class="col-sm-9 col-md-10">
    <input data-name="project" id="projectUsage<%= data.index %>Project" class="editor-input" value="<%= data.project %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-3 col-md-2">
    <label for="projectUsage<%= data.index %>Version">Version</label>
  </div>
  <div class="col-sm-3 col-md-4">
    <input data-name="version" id="projectUsage<%= data.index %>Version" class="editor-input" value="<%= data.version %>">
  </div>
  <div class="col-sm-2">
    <label for="projectUsage<%= data.index %>Date">Date</label>
  </div>
  <div class="col-sm-4">
    <input data-name="date" type="date" id="projectUsage<%= data.index %>Date" class="editor-input" value="<%= data.date %>">
  </div>
</div>