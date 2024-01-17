import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-2">
        <label class="control-label" for="supplemental<%= data.index %>Function">Type</label>
    </div>
    <div class="col-sm-10">
        <select data-name="function" class="editor-input function" id="supplemental<%= data.index %>Function">
            <option value="" selected>-- Choose one --</option>
            <option value="relatedArticle">RELATED ARTICLE. An article (or grey literature) which is relevant but which DOESN'T cite this resource</option>
            <option value="relatedDataset">RELATED DATASET. A dataset which is related but which is NOT hosted by EIDC)</option>
            <option value="website">RELATED WEBSITE. (e.g. project website)</option>
            <option value="">Miscellaneous</option>
        </select>
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        <label class="control-label" for="supplemental<%= data.index %>Name">Name</label>
    </div>
    <div class="col-sm-10">
        <input data-name='name' class="editor-input" id="supplemental<%= data.index %>Name" value="<%= data.name %>">
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        <label class="control-label" for="supplemental<%= data.index %>Description">Description</label>
    </div>
    <div class="col-sm-10">
        <textarea data-name='description' class="editor-textarea" id="supplemental<%= data.index %>Description" rows="4"><%= data.description %></textarea>
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        <label class="control-label" for="supplemental<%= data.index %>URL">URL/DOI</label>
    </div>
    <div class="col-sm-10">
        <input data-name='url' class="editor-input" id="supplemental<%= data.index %>URL" value="<%= data.url %>">
    </div>
</div>
`)
