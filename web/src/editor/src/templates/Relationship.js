import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-2 col-lg-2">
        <label for="relationship<%= data.index %>Relation">Relation</label>
    </div>
    <div class="col-sm-10 col-lg-10">
        <select data-name="relation" id="relationship<%= data.index %>Relation" class="relationshipList editor-input" value="<%= data.relation %>">
          <option value="">Choose a relationship</option>
        </select>

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
            <input data-name="target" id="relationship<%= data.index %>Target" class="editor-input read-only-identifier" value="<%= data.target %>" disabled>
        </div>
    </div>
</div>
`)
