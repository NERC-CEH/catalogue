<div class="col-sm-1 col-lg-1">
  <label for="resourceIdentifier<%= data.index %>Code">Code</label>
</div>
<div class="col-sm-4 col-lg-4">
  <input data-name="code" id="resourceIdentifier<%= data.index %>Code" class="editor-input" value="<%= data.code %>">
</div>
<div class="col-sm-2 col-lg-2">
  <label for="resourceIdentifier<%= data.index %>CodeSpace">Codespace</label>
</div>
<div class="col-sm-2 col-lg-2">
  <input data-name="codeSpace" id="resourceIdentifier<%= data.index %>CodeSpace" class="editor-input" value="<%= data.codeSpace %>">
</div>
<div class="col-sm-1 col-lg-1">
  <label for="resourceIdentifier<%= data.index %>Version">Version</label>
</div>
<div class="col-sm-1 col-lg-1">
  <input data-name="version" id="resourceIdentifier<%= data.index %>Version" class="editor-input" value="<%= data.version %>">
</div>
<div class="col-sm-1 col-lg-1">
  <% if (data.index === 'Add') { %>
    <button class="btn btn-primary btn-sm" id="resourceIdentifiersAddNewEidc">Generate</button>
  <% } %>
</div>