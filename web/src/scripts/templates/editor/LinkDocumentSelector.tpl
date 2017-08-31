<div class="form-inline" style="margin-top: 6px">
  <div class="form-group">
    <label for="linkedDocumentId">Linked Document Identifier</label>
    <input data-name="<%= data.modelAttribute %>" class="form-control" readonly id="linkedDocumentId" value="<%= data.value %>">
  </div>
</div>
<div class="form-inline" style="margin-top: 6px">
  <div class="form-group">
    <label for="catalogue">Catalogue</label>
    <select id="catalogue" class="form-control"></select>
  </div>
  <div class="input-group">
    <input placeholder="Search the catalogue" id="term" type="text" autocomplete="off" class="form-control" value="<%= data.term %>">
    <div class="input-group-btn">
      <button tabindex="-1" class="btn btn-success" type="button">
        <span class="fa fa-search"></span>
      </button>
    </div>
  </div>
<hr>
<div id="results"></div>