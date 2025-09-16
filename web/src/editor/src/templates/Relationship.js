import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-md-1">
        <label for="relationship<%= data.index %>Relation">Relation</label>
    </div>
    <div class="col-md-3">
        <select data-name="relation" id="relationship<%= data.index %>Relation" class="relationshipList" value="<%= data.relation %>">
        </select>
    </div>
    <div class="col-md-1">
        <label for="relationship<%= data.index %>Target">Target</label>
    </div>
    <div class="relationshipSearch col-md-7">
        <div>
            <input data-name="target" value="<%= data.target %>" id="relationship<%= data.index %>Target" class="form-control editor-input autocomplete" placeholder="Enter record ID or type to search…">
        </div>
    </div>
    <div class="relationshipRecord col-md-7 d-none">
        <div>
            <input data-name="target" id="relationship<%= data.index %>Target" class="editor-input read-only-identifier" value="<%= data.target %>" disabled>
        </div>
    </div>
</div>
`)
