import _ from 'underscore'

export default _.template(`
<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label">EPSG:</label>
  </div>
  <div class="col-sm-2 col-lg-2">
    <div class="input-group">
      <input data-name='epsgCode' class="editor-input" value="<%= data.epsgCode %>"  <%= data.disabled%>>
      <span class="input-group-btn">
        <button class="btn btn-default btn-sm addReprojection" type="button"  <%= data.disabled%>><span class="fa-solid fa-plus" aria-hidden="true"></span></button>
      </span>
    </div>
  </div>
  <div class="col-sm-1 col-lg-1">
    <label class="control-label">Path</label>
  </div>
  <div class="col-sm-8 col-lg-8">
    <input data-name='path' class="editor-input" value="<%= data.path %>"  <%= data.disabled%>>
  </div>
</div>

<div class="reprojections"></div>

<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label">Type</label>
  </div>

  <div class="col-sm-2 col-lg-2">
    <select data-name='type' class="editor-input"  <%= data.disabled%>>
    <% _.each(data.types, function(d) {%>
    <option value="<%=d.value%>" <%= _.isString(data.type) && d.value===data.type.toUpperCase() ? 'selected="selected"': '' %>><%=d.name%></option>
    <%});%>
    </select>
  </div>

  <div class="col-sm-1 col-lg-1">
    <label class="control-label">Styling</label>
  </div>
  <div class="col-sm-5 col-lg-5">
    <div class="btn-group" role="group">
      <button type="button" class="btn btn-sm btn-default" styleMode="features"  <%= data.disabled%>>Simple</button>
      <button type="button" class="btn btn-sm btn-default" styleMode="attributes"  <%= data.disabled%>>Classification</button>
    </div>
    <button class="editor-button-xs addAttribute" type="button"  <%= data.disabled%>>Define Attribute <span class="fa-solid fa-plus" aria-hidden="true"></span></button>
  </div>

  <div class="col-sm-3 col-lg-3">
    <div class="byte-box">
      <div class="row">
        <div class="col-sm-3 col-lg-3">
          <label class="control-label">Byte?</label>&nbsp;
        </div>
        <div class="col-sm-5 col-lg-5">
          <input data-name="bytetype" type="radio" name="bytetype" value="true"  <%= data.disabled%>> Yes
        </div>
        <div class="col-sm-4 col-lg-4">
          <input data-name="bytetype" type="radio" name="bytetype" value="false"  <%= data.disabled%>> No
        </div>
      </div>
    </div>
  </div>

</div>

<div class="styling-box features"></div>
<div class="styling-box attributes"></div>
`)
