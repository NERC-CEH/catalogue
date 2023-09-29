import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-1">
        <label class="control-label" for="files<%= data.index %>Name">Name</label>
    </div>
    <div class="col-sm-3">
        <input data-name='name' class="editor-input" id="files<%= data.index %>Name" value="<%= data.name %>">
    </div>
    <div class="col-sm-1">
        <label class="control-label" for="files<%= data.index %>Format">Format</label>
    </div>
    <div class="col-sm-3">
        <input data-name='format' class="editor-input" id="files<%= data.index %>Format" value="<%= data.format %>">
    </div>
    <div class="col-sm-1">
        <label class="control-label" for="files<%= data.index %>Size">Size</label>
    </div>
    <div class="col-sm-3">
        <input data-name='size' class="editor-input" id="files<%= data.index %>Size" value="<%= data.size %>" placeholder="e.g. 32Mb or 0.4Gb">
    </div>
</div>
`)
