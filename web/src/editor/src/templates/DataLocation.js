import _ from 'underscore'

export default _.template(`
<div class="datalocation">
  <div class="row">
    <div class="col-sm-1">
      <label for="<%= data.modelAttribute %><%= data.index %>Uid">Unique ID</label>
    </div>
    <div class="col-sm-11">
      <input data-name="uid" id="<%= data.modelAttribute %><%= data.index %>Uid" class="editor-input" value="<%= data.uid %>">
    </div>
  </div>
  <div class="row">
    <div class="col-sm-1">
      <label for="<%= data.modelAttribute %><%= data.index %>Name">Name</label>
    </div>
    <div class="col-sm-11">
      <input data-name="name" id="<%= data.modelAttribute %><%= data.index %>Name" class="editor-input" value="<%= data.name %>"  placeholder="e.g. Post-CAP">
    </div>
  </div>
  <div class="row">
    <div class="col-sm-1">
      <label for="<%= data.modelAttribute %><%= data.index %>Purpose">Purpose</label>
    </div>
    <div class="col-sm-11">
      <input data-name="purpose" id="<%= data.modelAttribute %><%= data.index %>Purpose" class="editor-input" value="<%= data.purpose %>" placeholder="e.g. This datafile contains data for the Post-CAP scenario">
    </div>
  </div>
  <div class="row">
    <div class="col-sm-1">
      <label for="<%= data.modelAttribute %><%= data.index %>FileLocation">File location</label>
    </div>
    <div class="col-sm-11">
      <input data-name="fileLocation" id="<%= data.modelAttribute %><%= data.index %>FileLocation" class="editor-input" value="<%= data.fileLocation %>" placeholder="e.g. //server/folder/file.csv OR https://ishare.com/data/geodatabase.gdb">
    </div>
  </div>
</div>
`)
