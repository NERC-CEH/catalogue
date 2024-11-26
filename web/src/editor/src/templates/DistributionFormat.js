import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-md-1">
        <label for="distributionFormat<%= data.index %>Name">Name</label>
    </div>
    <div class="col-md-4">
        <input data-name='name' class="editor-input" id="distributionFormat<%= data.index %>Name" value="<%= data.name %>">
    </div>
    <div class="col-md-1">
        <label for="distributionFormat<%= data.index %>Type">Type</label>
    </div>
    <div class="col-md-3">
        <input data-name='type' class="editor-input" id="distributionFormat<%= data.index %>Type" value="<%= data.type %>">
    </div>
    <div class="col-md-1">
        <label for="distributionFormat<%= data.index %>Version">Version</label>
    </div>
    <div class="col-md-2">
        <input data-name='version' class="editor-input" id="distributionFormat<%= data.index %>Version" value="<%= data.version %>">
    </div>
</div>
`)
