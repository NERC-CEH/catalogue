<h4>Attribute Definition</h4>
<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label">Name</label>
  </div>
  <div class="col-sm-7 col-lg-7">
    <input data-name='id' class="editor-input" value="<%= data.id %>"  <%= data.disabled%>>
  </div>
  <div class="col-sm-2 col-lg-2">
    <label class="control-label">Data type</label>
  </div>
  <div class="col-sm-2 col-lg-2">
    <select data-name='type' class="editor-input"  <%= data.disabled%>>
      <% _.each(data.types, function(d) {%>
        <option value="<%=d.value%>" <%= d.value==data.type ? 'selected="selected"': '' %>><%=d.name%></option>
      <%});%>
    </select>
  </div>
</div>
<h4>Layer Definition</h4>
<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label">ID</label>
  </div>
  <div class="col-sm-3 col-lg-3">
    <input data-name='name' class="editor-input" value="<%= data.name %>"  <%= data.disabled%>>
  </div>
  <div class="col-sm-1 col-lg-1">
    <label class="control-label">Title</label>
  </div>
  <div class="col-sm-7 col-lg-7">
    <input data-name='label' class="editor-input" value="<%= data.label %>"  <%= data.disabled%>>
  </div>
</div>

<hr/>
<h4>Values <button class="btn btn-default btn-xs addValue"  <%= data.disabled%>>Add <span class="fas fa-plus"></span></button></h4>
<div class="values"></div>
<hr/>
<h4>Buckets <button class="btn btn-default btn-xs addBucket"  <%= data.disabled%>>Add <span class="fas fa-plus"></span></button></h4>
<div class="buckets"></div>