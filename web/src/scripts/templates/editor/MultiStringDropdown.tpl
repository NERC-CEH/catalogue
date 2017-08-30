<div class="row" id="input<%= data.modelAttribute %><%= data.index %>">
  <div class="col-sm-11 col-lg-11 dataentry">
    <select id= "select<%= data.modelAttribute %><%= data.index %>" data-index="<%= data.index %>" data-name="value" class="editor-input">
      <% _.each(this.data.options, function(option) { %>
          <option value="<%= option.value %>"><%= option.label %></option>
      <% }); %>
    </select>
  </div>
  <div class="col-sm-1 col-lg-1">
    <button data-index="<%= data.index %>" class="editor-button remove"><i class="fa fa-times"></i></button>
  </div>
</div>