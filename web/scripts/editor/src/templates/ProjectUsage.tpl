<div class="row">
   <div class="col-sm-1">
    <label for="projectUsage<%= data.index %>Project">Project</label>
  </div>
  <div class="col-sm-5 col-lg-5">
    <input data-name="project" id="projectUsage<%= data.index %>Project" class="editor-input" value="<%= data.project %>">
  </div>
  <div class="col-sm-1">
    <label for="projectUsage<%= data.index %>Version">Version</label>
  </div>
  <div class="col-sm-2">
    <input data-name="version" id="projectUsage<%= data.index %>Version" class="editor-input" value="<%= data.version %>">
  </div>
  <div class="col-sm-1">
    <label for="projectUsage<%= data.index %>Date">Date</label>
  </div>
  <div class="col-sm-2">
    <input data-name="date" type="date" id="projectUsage<%= data.index %>Date" class="editor-input" value="<%= data.date %>">
  </div>
</div>