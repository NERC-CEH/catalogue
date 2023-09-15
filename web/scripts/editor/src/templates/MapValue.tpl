<div class="row">
  <div class="col-sm-1">
    <label class="control-label">Label</label>
  </div>
  <div class="col-sm-3 col-lg-3">
    <input data-name='label' class="editor-input" value="<%= data.label %>" <%= data.disabled%>>
  </div>

  <div class="col-sm-1">
    <label class="control-label">Setting</label>
  </div>
  <div class="col-sm-3 col-lg-3">
    <input data-name='setting' class="editor-input" value="<%= data.setting %>" <%= data.disabled%>>
  </div>

  <div class="col-sm-1">
    <label class="control-label">Style</label>
  </div>
  <div class="col-sm-3 col-lg-3 style-selector"></div>
</div>