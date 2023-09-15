<div class="row">
  <div class="col-sm-1">
    <label class="control-label">Label</label>
  </div>
  <div class="col-sm-3 col-lg-3">
    <input data-name='label' class="editor-input" value="<%= data.label %>" <%= data.disabled%>>
  </div>

  <div class="col-sm-1">
    <label class="control-label">Bucket</label>
  </div>
  <div class="col-sm-3 col-lg-3">
    <div class="input-group">
      <input data-name='min' class="form-control editor-input" value="<%= data.min %>" placeholder="-∞" <%= data.disabled%>>
      <span class="input-group-addon">'&#60 𝑥 &#8804</span>
      <input data-name='max' class="form-control editor-input" value="<%= data.max %>" placeholder="∞" <%= data.disabled%>>
    </div>
  </div>

  <div class="col-sm-1">
    <label class="control-label">Style</label>
  </div>
  <div class="col-sm-3 col-lg-3 style-selector"></div>
</div>