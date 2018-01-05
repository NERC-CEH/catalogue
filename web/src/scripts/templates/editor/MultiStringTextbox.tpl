<div class="row" id="input<%= data.modelAttribute %><%= data.index %>">
  <div class="col-sm-11 col-lg-11 dataentry">
    <textarea data-index="<%= data.index %>" rows="<%= data.rows %>" class="editor-input editor-textarea"><%= data.value %></textarea>
  </div>
  <div class="col-sm-1 col-lg-1">
    <button data-index="<%= data.index %>" class="editor-button remove"><i class="fa fa-times"></i></button>
  </div>
</div>