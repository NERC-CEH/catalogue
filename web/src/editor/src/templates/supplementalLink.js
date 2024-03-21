import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-lg-1 col-sm-2">
        <label class="control-label" for="supplementalLink<%= data.index %>Description">Description</label>
    </div>
    <div class="col-lg-11 col-sm-10">
        <textarea data-name='description' class="editor-textarea" id="supplementalLink<%= data.index %>Description" rows="3"><%= data.description %></textarea>
    </div>
</div>
<div class="row">
    <div class="col-lg-1 col-sm-2">
        <label class="control-label" for="supplementalLink<%= data.index %>URL">DOI/URL</label>
    </div>
    <div class="col-lg-11 col-sm-10">
        <input data-name='url' class="editor-input" id="supplementalLink<%= data.index %>URL" value="<%= data.url %>">
    </div>
</div>
`)
