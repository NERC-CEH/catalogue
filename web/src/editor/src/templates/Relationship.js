import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-md-5">
        <select data-name="relation" id="relationship<%= data.index %>Relation" class="styledSelect relationshipList" value="<%= data.relation %>">
            <button>
              <selectedcontent></selectedcontent>
            </button>
        </select>
    </div>
    <div class="relationshipSearch col-md-7">
        <input data-name="target" value="<%= data.target %>" id="relationship<%= data.index %>Target" class="editor-input autocomplete" placeholder="Enter record ID or type to search…">
    </div>
    <div class="relationshipRecord col-md-7 d-none">
        <input data-name="target" id="relationship<%= data.index %>Target" class="editor-input read-only-identifier" value="<%= data.target %>" disabled>
    </div>
</div>
`)
