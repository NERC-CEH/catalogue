import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-sm-2">
        <label for="contactPerson">Person</label>
    </div>
    <div class="col-sm-10">
        <input data-name='fullName' class="editor-input" id="contactPerson" value="<%= data.fullName %>">
    </div>
</div>
<div class="row">
    <div class="col-sm-2">
        <label for="contactOrganisation">Organisation</label>
    </div>
    <div class="col-sm-10">
        <input data-name='organisationName' class="editor-input" id="contactOrganisation" value="<%= data.organisationName %>">
    </div>
</div>
`)
