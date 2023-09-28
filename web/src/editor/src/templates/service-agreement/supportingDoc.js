import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-2">
        <label class="control-label" for="supportingDocs<%= data.index %>Filename">Filename</label>
    </div>
    <div class="col-sm-10">
        <input data-name='filename' class="editor-input" id="supportingDocs<%= data.index %>Filename" value="<%= data.filename %>">
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
