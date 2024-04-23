import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-1">
        <label class="control-label" for="supportingDocs<%= data.index %>Name">Name</label>
    </div>
    <div class="col-sm-3">
        <input data-name="name" class="editor-input" id="supportingDocs<%= data.index %>Name" value="<%= data.name %>">
    </div>
    <div class="col-sm-1">
        <label class="control-label" for="supportingDocs<%= data.index %>Format">Format</label>
    </div>
    <div class="col-sm-3">
        <input data-name="format" class="editor-input autocomplete" id="supportingDocs<%= data.index %>Format" value="<%= data.format %>">
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        <label class="control-label" for="supportingDocs<%= data.index %>Content">Content</label>
    </div>
    <div class="col-sm-10">
        <textarea data-name="content" id="supportingDocs<%= data.index %>Content" class="editor-textarea" rows="5"><%= data.content %></textarea>
    </div>
</div>
`)
