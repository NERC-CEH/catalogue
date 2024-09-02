import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-lg-1 col-sm-2">
        <label class="control-label" for="supplemental<%= data.index %>Description">Details</label>
    </div>
    <div class="col-lg-11 col-sm-10">
        <textarea data-name='description' class="editor-textarea" id="supplemental<%= data.index %>Description" rows="3"><%= data.description %></textarea>
    </div>
</div>
<div class="row">
    <div class="col-lg-1 col-sm-2">
        <label class="control-label" for="supplemental<%= data.index %>URL">DOI/URL</label>
    </div>
    <div class="col-lg-11 col-sm-10">
        <input data-name='url' class="editor-input" id="supplemental<%= data.index %>URL" value="<%= data.url %>">
    </div>
</div>
<div class="row">
    <div class="col-lg-1 col-sm-2">
        <label class="control-label" for="supplemental<%= data.index %>Type">Type</label>
    </div>
    <div class="col-lg-11 col-sm-10">
        <select data-name='type' class="editor-input" id="supplemental<%= data.index %>Type">
            <option value="">- Choose type -</option>
            <option value="academic">Academic</option>
            <option value="policy">Policy</option>
        </select>
    </div>
</div>
`)
