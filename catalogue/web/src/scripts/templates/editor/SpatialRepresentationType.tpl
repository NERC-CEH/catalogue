<div class="row" id="input<%= data.modelAttribute %><%= data.index %>">
  <div class="col-sm-11 col-lg-11 dataentry">
    <select data-index="<%= data.index %>" class="editor-input">
      <option value="">- Select Spatial Representation Type -</option>
      <option value="grid">grid</option>
      <option value="stereoModel">stereoModel</option>
      <option value="textTable">textTable</option>
      <option value="tin">tin</option>
      <option value="vector">vector</option>
      <option value="video">video</option>
    </select>
  </div>
  <div class="col-sm-1 col-lg-1">
    <button data-index="<%= data.index %>" class="editor-button remove"><i class="glyphicon glyphicon-remove"></i></button>
  </div>
</div>