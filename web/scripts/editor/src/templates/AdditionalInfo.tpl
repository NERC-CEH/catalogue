<div class="row">
  <div class="col-sm-1">
    <label for="qa<%= data.index %>Key">Key</label>
  </div>
  <div class="col-sm-11">
    <input data-name="key" type="text" id="qa<%= data.index %>Key" class="editor-input" value="<%= data.key %>">
  </div>
  <div class="col-sm-1">
    <label for="qa<%= data.index %>Value">Value</label>
  </div>
  <div class="col-sm-11">
    <textarea data-name="value" id="qa<%= data.index %>Value" class="form-control" rows="2"><%= data.value %></textarea>
  </div>
</div>
