import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-2">
        <label for="contacts<%= data.index %>Organisation">Affiliation</label>
    </div>
    <div class="col-sm-10">
        <input data-name='organisationName' class="editor-input" id="contacts<%= data.index %>Organisation" value="<%= data.organisationName %>">
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        <label for="contacts<%= data.index %>organisationIdentifier">Organisation's RoR</label>
    </div>
    <div class="col-sm-4">
        <input data-name='organisationIdentifier' class="editor-input" id="contacts<%= data.index %>organisationIdentifier" value="<%= data.organisationIdentifier %>">
    </div>
    <div class="col-sm-2">
        <label for="contacts<%= data.index %>Name">Name</label>
    </div>
    <div class="col-sm-4">
        <input data-name='individualName' class="editor-input" id="contacts<%= data.index %>Name" value="<%= data.individualName %>">
    </div>
</div>
`)
