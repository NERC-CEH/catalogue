<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label">Type</label>
  </div>

  <div class="col-sm-2 col-lg-2">
    <select data-name='type' class="editor-input">
      <% _.each(data.types, function(d) {%>
        <option value="<%=d.value%>" <%= d.value==data.type ? 'selected="selected"': '' %>><%=d.name%></option>
      <%});%>
    </select>
  </div>
  <div class="col-sm-1 col-lg-1">
    <label class="control-label">EPSG:</label>
  </div>
  <div class="col-sm-2 col-lg-2">
    <div class="input-group">
      <input data-name='epsgCode' class="editor-input" value="<%= data.epsgCode %>">
      <span class="input-group-btn">
        <button class="btn btn-default btn-sm addReprojection" type="button"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span></button>
      </span>
    </div>

  </div>
  <div class="col-sm-1 col-lg-1">
    <label class="control-label">Path</label>
  </div>
  <div class="col-sm-5 col-lg-5">
    <input data-name='path' class="editor-input" value="<%= data.path %>">
  </div>
</div>

<div class="reprojections"></div>
<div class="features"></div>

<button class="btn btn-default btn-xs addAttribute" type="button">Define Attribute <span class="glyphicon glyphicon-plus" aria-hidden="true"></span></button>
<div class="attributes existing"></div>
