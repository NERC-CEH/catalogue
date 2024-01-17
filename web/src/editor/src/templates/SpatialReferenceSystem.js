import _ from 'underscore'

export default _.template(`
<div class="col-sm-2">
    <label for="spatialReferenceSystem<%= data.index %>Title">Title</label>
</div>
<div class="col-sm-4">
    <input data-name="title" id="spatialReferenceSystem<%= data.index %>Title" class="editor-input" value="<%= data.title %>">
</div>
<div class="col-sm-2">
    <label for="spatialReferenceSystem<%= data.index %>Code">Code</label>
</div>
<div class="col-sm-4">
    <input data-name="code" id="spatialReferenceSystem<%= data.index %>Code" class="editor-input" value="<%= data.code %>">
</div>
`)
