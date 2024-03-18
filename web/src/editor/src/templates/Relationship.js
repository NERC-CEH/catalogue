import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-2 col-lg-2">
        <label for="relationship<%= data.index %>Relation">Relation</label>
    </div>
    <div class="col-sm-10 col-lg-10">
        <input list="relationship<%= data.index %>relationships" data-name="relation" id="relationship<%= data.index %>Relation" class="editor-input" value="<%= data.relation %>">
        <datalist id="relationship<%= data.index %>relationships"></datalist>
    </div>
</div>
<div class="row">
    <div class="col-sm-2 col-lg-2">
        <label for="relationship<%= data.index %>Target">Target</label>
    </div>
    <div class="relationshipSearch">
        <div class="col-sm-10 col-lg-10">
            <input data-name="target" value="<%= data.target %>" id="relationship<%= data.index %>Target" class="form-control editor-input autocomplete" placeholder="Search the catalogue...">
        </div>
    </div>
    <div class="relationshipRecord hidden">
        <div class="col-sm-10 col-lg-10">
            <input data-name="target" id="relationship<%= data.index %>Target" class="editor-input read-only-identifier" value="<%= data.target %>" disabled="true">
        </div>
    </div>
</div>
`)
