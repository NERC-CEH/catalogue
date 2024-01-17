import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-2 col-lg-2">
        <label for="versionHistory<%= data.index %>Version">Version</label>
    </div>
    <div class="col-sm-4 col-lg-4">
        <input data-name="version" id="versionHistory<%= data.index %>Version" class="editor-input" value="<%= data.version %>">
    </div>
    <div class="col-sm-2 col-lg-2">
        <label for="versionHistory<%= data.index %>Date">Date</label>
    </div>
    <div class="col-sm-4 col-lg-4">
        <input data-name="date" type="date" id="versionHistory<%= data.index %>Date" class="editor-input" value="<%= data.date %>">
    </div>
</div>
<div class="row">
    <div class="col-sm-2 col-lg-2">
        <label for="versionHistory<%= data.index %>Note">Note</label>
    </div>
    <div class="col-sm-10 col-lg-10">
        <textarea rows="5" data-name="note" id="versionHistory<%= data.index %>Note" class="editor-textarea"><%= data.note %></textarea>
    </div>
</div>
`)
