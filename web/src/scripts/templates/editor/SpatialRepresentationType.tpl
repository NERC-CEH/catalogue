<div class="row" id="input<%= data.modelAttribute %><%= data.index %>">
  <div class="col-sm-11 col-lg-11 dataentry">
    <select data-index="<%= data.index %>" class="editor-input">
      <option value="">- Select Spatial Representation Type -</option>
      <option value="grid">Raster (grid)</option>
      <option value="stereoModel">stereoModel (a three-dimensional view formed by overlapping pairs of images)</option>
      <option value="textTable">Tabular data (e.g. a spreadsheet)</option>
      <option value="tin">Triangular Irregular Network</option>
      <option value="vector">Vector (e.g. Shape file)</option>
      <option value="video">Video</option>
    </select>
  </div>
  <div class="col-sm-1 col-lg-1">
    <button data-index="<%= data.index %>" class="editor-button remove"><i class="fa fa-times"></i></button>
  </div>
</div>
