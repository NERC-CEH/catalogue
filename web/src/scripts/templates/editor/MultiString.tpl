<div class="row" id="input<%= data.modelAttribute %><%= data.index %>">
  <div class="col-sm-11 col-lg-11 dataentry">
    <input data-index="<%= data.index %>" class="editor-input" value="<%= data.value %>">
  </div>
  <div class="col-sm-1 col-lg-1">
    <button data-index="<%= data.index %>" class="editor-button remove"><i class="fas fa-times"></i></button>
  </div>
</div>