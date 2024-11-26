import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-2">
        <label for="supplemental<%= data.index %>Name">Name</label>
    </div>
    <div class="col-sm-10">
        <input data-name='name' class="editor-input" id="supplemental<%= data.index %>Name" value="<%= data.name %>">
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        <label for="supplemental<%= data.index %>Description">Description</label>
    </div>
    <div class="col-sm-10">
        <textarea data-name='description' class="editor-textarea" id="supplemental<%= data.index %>Description" rows="4"><%= data.description %></textarea>
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        <label for="supplemental<%= data.index %>URL">URL/DOI</label>
    </div>
    <div class="col-sm-10">
        <input data-name='url' class="editor-input" id="supplemental<%= data.index %>URL" value="<%= data.url %>">
    </div>
</div>
`)
