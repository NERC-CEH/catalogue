<div class="form-inline" style="margin-top: 6px">
  <div class="form-group">
    <label for="linkedDocumentId">Linked Document Identifier</label>
    <input data-name="<%= data.modelAttribute %>" class="form-control" id="linkedDocumentId" value="<%= data.value %>">
  </div>
</div>
<div class="form-inline" style="margin-top: 6px">
  <div class="form-group">
    <label for="catalogue">Catalogue</label>
    <select id="catalogue" class="form-control">
      <option value="assist">Achieving Sustainable Agricultural Systems</option>
      <option value="ceh">Centre for Ecology &amp; Hydrology</option>
      <option value="cmp">Catchment Management Platform</option>
      <option value="eidc" selected>Environmental Information Data Centre</option>
    </select>
  </div>
  <div class="input-group">
    <input placeholder="Search the catalogue" id="term" type="text" autocomplete="off" class="form-control">
    <div class="input-group-btn">
      <button tabindex="-1" class="btn btn-success" type="button">
        <span class="glyphicon glyphicon-search"></span>
      </button>
    </div>
  </div>
<hr>
<div id="results"></div>