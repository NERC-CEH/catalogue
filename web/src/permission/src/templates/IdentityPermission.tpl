<td><%= identity %></td>
<td><input data-permission="canView" type="checkbox" <% if(canView) { %>checked<% } %>></td>
<td><input data-permission="canEdit" type="checkbox" <% if(canEdit) { %>checked<% } %>></td>
<td><input data-permission="canDelete" type="checkbox" <% if(canDelete) { %>checked<% } %>></td>
<td><input data-permission="canUpload" type="checkbox" <% if(canUpload) { %>checked<% } %>></td>
<td><button class="editor-button-xs"><i class="fas fa-times"></i></button></td>