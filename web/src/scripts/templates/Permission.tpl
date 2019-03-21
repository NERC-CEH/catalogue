<h2>Permissions</h2>
<div class="alert alert-info">
  <i class="fas fa-info-circle fa-2x pull-right"></i> For external users, the username is the email address with which they registered an account.<br>For CEH staff it is their CEH login username <b>not</b> their email address.
</div>
<table class="table table-striped">
  <thead>
  <tr>
    <th>Username/Group</th>
    <th>Can View</th>
    <th>Can Edit</th>
    <th>Can Delete</th>
    <th>Can Upload</th>
    <th>Action</th>
  </tr>
  </thead>
  <tfoot>
    <tr>
      <td><input id="identity" class="form-control" placeholder="username/group"></td>
      <td><input id="canView" type="checkbox"></td>
      <td><input id="canEdit" type="checkbox"></td>
      <td><input id="canDelete" type="checkbox"></td>
      <td><input id="canUpload" type="checkbox"></td>
      <td><button id="permissionAdd" class="editor-button-xs">Add</button></td>
    </tr>
  </tfoot>
  <tbody>
  </tbody>
</table>
<div class="navbar navbar-default navbar-fixed-bottom">
  <div class="container">
    <div class="navbar-right">
      <a href="/documents/<%= id %>/permission" class="btn btn-default navbar-btn">Cancel</a>
      <button id="permissionSave" class="btn btn-primary navbar-btn"><i class="far fa-save"></i> Save</button>
    </div>
  </div>
</div>