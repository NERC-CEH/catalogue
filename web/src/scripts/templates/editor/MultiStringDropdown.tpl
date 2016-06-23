<div class="row" id="input<%= data.modelAttribute %><%= data.index %>">
  <div class="col-sm-11 col-lg-11 dataentry">
    <select id= "select<%= data.modelAttribute %><%= data.index %>" data-index="<%= data.index %>" data-name="value" class="editor-input">
      <option value="" selected>- Select Catalogue -</option>
      <option value="Catchment Management Platform">Catchment Management Platform</option>
      <option value="Environmental Information Data Centre">Environmental Information Data Centre</option>
    </select>
  </div>
  <div class="col-sm-1 col-lg-1">
    <button data-index="<%= data.index %>" class="editor-button remove"><i class="glyphicon glyphicon-remove"></i></button>
  </div>
</div>