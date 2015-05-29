<div class="col-sm-1 col-lg-1">
  <label for="resourceIdentifier<%= index %>Code">Code</label>
</div>
<div class="col-sm-5 col-lg-5">
  <input data-name="code" id="resourceIdentifier<%= index %>Code" class="editor-input" value="<%= code %>">
</div>
<div class="col-sm-1 col-lg-1">
  <label for="resourceIdentifier<%= index %>CodeSpace">Codespace</label>
</div>
<div class="col-sm-2 col-lg-2">
  <input data-name="codeSpace" id="resourceIdentifier<%= index %>CodeSpace" class="editor-input" value="<%= codeSpace %>">
</div>
<div class="col-sm-1 col-lg-1">
  <label for="resourceIdentifier<%= index %>Version">Version</label>
</div>
<div class="col-sm-1 col-lg-1">
  <input data-name="version" id="resourceIdentifier<%= index %>Version" class="editor-input" value="<%= version %>">
</div>
<div class="col-sm-1 col-lg-1">
  <% if (index === 'Add') { %>
    <button class="editor-button add">Add</button>
  <% } else { %>
    <button class="editor-button remove"><i class="glyphicon glyphicon-remove"></i></button>
  <% } %>
</div>