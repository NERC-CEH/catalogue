import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-2 required">
        <label for="contacts<%= data.index %>Name">
          Name
        </label>
    </div>
    <div class="col-sm-4 required">
        <input data-name='individualName' class="editor-input" id="contacts<%= data.index %>Name" value="<%= data.individualName %>">
    </div>
    <div class="col-sm-2 required">
        <label for="contacts<%= data.index %>Email">
          Email
        </label>
    </div>
    <div class="col-sm-4 required">
        <input data-name='email' class="editor-input" id="contacts<%= data.index %>Email" value="<%= data.email %>">
    </div>
</div>
<div class="row">
    <div class="col-sm-2 required">
        <label for="contacts<%= data.index %>Organisation">
          Affiliation
        </label>
    </div>
    <div class="col-sm-4 required">
        <input data-name='organisationName' class="editor-input" id="contacts<%= data.index %>Organisation" value="<%= data.organisationName %>">
    </div>

    <div class="col-sm-2">
        <label for="contacts<%= data.index %>nameIdentifier">ORCID</label>
    </div>
    <div class="col-sm-4">
        <input data-name='nameIdentifier' placeholder='https://orcid.org/0000-0000-0000-0000' class="editor-input" id="contacts<%= data.index %>nameIdentifier" value="<%= data.nameIdentifier %>">
    </div>
</div>
`)
