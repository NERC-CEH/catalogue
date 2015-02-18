<h1><%= title %></h1>
<h2>Permissions</h2>
<table class="table table-striped">
  <thead>
  <tr>
    <th>Username/Group</th>
    <th>Can View</th>
    <th>Can Edit</th>
    <th>Can Delete</th>
    <th>Action</th>
  </tr>
  </thead>
  <tfoot>
    <tr>
      <td><input class="form-control" placeholder="username/group"></td>
      <td><input type="checkbox"></td>
      <td><input type="checkbox"></td>
      <td><input type="checkbox"></td>
      <td><button id="permissionAdd" class="btn btn-default btn-xs">Add</button></td>
    </tr>
  </tfoot>
  <tbody>
  <% _.each(permissions, function(permission) { %>
    <tr>
      <td><%= permission.identity %></td>
      <td><input type="checkbox" <% if(permission.canView) { %>checked<% } %>></td>
      <td><input type="checkbox" <% if(permission.canEdit) { %>checked<% } %>></td>
      <td><input type="checkbox" <% if(permission.canDelete) { %>checked<% } %>></td>
      <td><button class="btn btn-default btn-xs"><i class="glyphicon glyphicon-remove"></i></button></td>
    </tr>
  <% }); %>
  </tbody>
</table>
<div class="navbar navbar-default navbar-fixed-bottom">
  <div class="container">
    <div class="navbar-right">
      <a href="<%= href %>" class="btn btn-default navbar-btn">Cancel</a>
      <button id="permissionSave" class="btn btn-primary navbar-btn">Save <i class="glyphicon glyphicon-save"></i></button>
    </div>
  </div>
</div>