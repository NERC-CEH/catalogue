import _ from 'underscore'

export default _.template(`
        <% if(typeof doctype === "undefined"){doctype = "documents"} %>
        <h2><% if(doctype === "service-agreement"){ %>Service Agreement <% } %>Permissions</h2>
<div>
    <p><i class="fa-solid fa-info-circle fa-2x pull-right"></i>Amend permissions for users or groups. For external users, the username is the email address with which they registered an account.<br>For UKCEH staff it is their login username <b>not</b> their email address.</p>
    <p><b>NOTE</b>: Catalogue administrators have permission to add and edit all records regardless of permissions defined here.</p>
</div>
<table class="table table-striped">
    <thead>
    <tr>
        <th>Username/Group</th>
        <th>Can view</th>
        <th>Can edit</th>
        <% if(doctype !== "service-agreement") {%>
        <th>Can delete</th>
        <th>Can upload</th>
        <% } %>
        <th>Action</th>
    </tr>
    </thead>
    <tfoot>
        <tr>
            <td><input id="identity" class="form-control" placeholder="username/group"></td>
            <td><input id="canView" type="checkbox"></td>
            <td><input id="canEdit" type="checkbox"></td>
            <% if(doctype !== "service-agreement") {%>
            <td><input id="canDelete" type="checkbox"></td>
            <td><input id="canUpload" type="checkbox"></td>
            <% } %>
            <td><button id="permissionAdd" class="editor-button-xs" title="remove permissions for this user">Add</button></td>
        </tr>
    </tfoot>
    <tbody>
    </tbody>
</table>
<div class="navbar navbar-default navbar-fixed-bottom">
    <div class="container">
        <div class="navbar-right">
            <a href="/<%= doctype %>/<%= id %>/permission" class="btn btn-default navbar-btn">Cancel</a>
            <button id="permissionSave" class="btn btn-primary navbar-btn"><i class="fa-regular fa-save"></i> Save</button>
        </div>
    </div>
</div>
`)
