<div class="row">
  <div class="col-sm-1">
      <label for="resolution<%= data.index %>Category">Category</label>
  </div>
  <div class="col-sm-11">
      <select data-name="category" class="editor-input category" id="resolution<%= data.index %>Category">
        <option value="" selected>-- Choose one --</option>
        <option value="temporal">Temporal</option>
        <option value="vertical">Vertical</option>
        <option value="horizontal">Horizontal</option>
      </select>
  </div>

  <div class="col-sm-1">
    <label for="resolution<%= data.index %>Min">Min</label>
  </div>
  <div class="col-sm-5">
    <input data-name="min" id="resolution<%= data.index %>Min" class="editor-input" value="<%= data.min %>">
  </div>

  <div class="col-sm-1">
    <label for="resolution<%= data.index %>Max">Max</label>
  </div>
  <div class="col-sm-5">
    <input data-name="max" id="resolution<%= data.index %>Max" class="editor-input" value="<%= data.max %>">
  </div>

</div>
