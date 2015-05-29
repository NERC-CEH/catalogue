<div class="col-sm-11 col-lg-11">
  <input data-name="value" class="editor-input" value="<%= value %>">
</div>
<div class="col-sm-1 col-lg-1">
  <% if (index === 'Add') { %>
    <button class="editor-button add">Add</button>
  <% } else { %>
    <button class="editor-button remove"><i class="glyphicon glyphicon-remove"></i></button>
  <% } %>
</div>