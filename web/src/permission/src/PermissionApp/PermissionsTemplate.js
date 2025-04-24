import _ from 'underscore'

export default _.template(`
        <% if(typeof doctype === "undefined"){doctype = "documents"} %>
        <div class="container">
       <h1 class="mt-4"><% if(doctype === "service-agreement"){ %>Service Agreement <% } %>Permissions</h1>
<div class="mb-4">
    <p><i class="fa-solid fa-info-circle fa-2x float-end"></i>Amend permissions for users or groups.</p>
    <p>For external users, the username is the email address with which they registered an account.<br>For UKCEH staff it is their login username <b>not</b> their email address.</p>
    <p><b>NOTE</b>: Catalogue administrators have permission to edit all records regardless of permissions defined here.</p>
</div>
<table class="table table-bordered">

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
            <td><button id="permissionAdd" class="btn btn-light border btn-sm" title="remove permissions for this user">Add</button></td>
        </tr>
    </tfoot>
    <tbody>
    </tbody>
  </table>
</div>
<div class="navbar fixed-bottom p-3 bg-secondary">
    <div class="container">
        <div class="d-flex ms-auto">
            <a href="/<%= doctype %>/<%= id %>/permission" class="btn btn-light border me-2">Cancel</a>
            <button id="permissionSave" class="btn btn-primary"><i class="fa-regular fa-save"></i> Save</button>
        </div>
    </div>
</div>
`)
