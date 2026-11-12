import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-1">
        <label for="fileGroups<%- data.index %>Pattern">Pattern</label>
    </div>
    <div class="col-sm-5">
        <input data-name='pattern' class="editor-input" id="fileGroups<%- data.index %>Pattern" value="<%- data.pattern %>" placeholder="e.g. site1/*.csv">
    </div>
    <div class="col-sm-1">
        <label for="fileGroups<%- data.index %>Count">Files</label>
    </div>
    <div class="col-sm-2">
        <input data-name='count' class="editor-input" id="fileGroups<%- data.index %>Count" value="<%- data.count %>" inputmode="numeric" placeholder="e.g. 200">
    </div>
</div>
<div class="row">
    <div class="col-sm-1">
        <label for="fileGroups<%- data.index %>Format">Format</label>
    </div>
    <div class="col-sm-3">
        <input data-name='format' class="editor-input" id="fileGroups<%- data.index %>Format" value="<%- data.format %>">
    </div>
    <div class="col-sm-1">
        <label for="fileGroups<%- data.index %>Size">Size</label>
    </div>
    <div class="col-sm-3">
        <input data-name='size' class="editor-input" id="fileGroups<%- data.index %>Size" value="<%- data.size %>" placeholder="e.g. 2-2.5 MB each, 443 MB in total">
    </div>
</div>
<div class="row">
    <div class="col-sm-1">
        <label for="fileGroups<%- data.index %>Description">Description</label>
    </div>
    <div class="col-sm-11">
        <input data-name='description' class="editor-input" id="fileGroups<%- data.index %>Description" value="<%- data.description %>">
    </div>
</div>
`)
