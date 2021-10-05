<div class="row">
    <div class="col-sm-2">
        <label class="control-label" for="contacts<%= data.index %>Role">Role</label>
    </div>
    <div class="col-sm-10">
        <select data-name="role" class="editor-input" id="contacts0Role" >
            <option value="author" selected="selected" >Author</option>
        </select>
    </div>
</div>

<div class="col-sm-10 col-sm-offset-2 hidden-xs"><hr></div>

<div class="row">
    <div class="col-sm-2">
        <label class="control-label" for="contacts<%= data.index %>Name">Name</label>
    </div>
    <div class="col-sm-4">
        <input data-name='individualName' class="editor-input" id="contacts<%= data.index %>Name" value="<%= data.individualName %>">
    </div>
    <div class="col-sm-1">
        <label class="control-label" for="contacts<%= data.index %>nameIdentifier">ORCID</label>
    </div>
    <div class="col-sm-5">
        <input data-name='nameIdentifier' placeholder='https://orcid.org/0000-...' class="editor-input" id="contacts<%= data.index %>nameIdentifier" value="<%= data.nameIdentifier %>">
    </div>
</div>

<div class="col-sm-10 col-sm-offset-2 hidden-xs"><hr></div>

<div class="row">
    <div class="col-sm-2">
        <label class="control-label" for="contacts<%= data.index %>Email">Email</label>
    </div>
    <div class="col-sm-10">
        <input data-name='email' class="editor-input" id="contacts<%= data.index %>Email" value="<%= data.email %>">
    </div>
</div>

<div class="col-sm-10 col-sm-offset-2 hidden-xs"><hr></div>

<div class="row">
    <div class="col-sm-2">
        <label class="control-label" for="contacts<%= data.index %>Organisation">Organisation</label>
    </div>
    <div class="col-sm-10">
        <input data-name='organisationName' class="editor-input" id="contacts<%= data.index %>Organisation" value="<%= data.organisationName %>">
    </div>
</div>