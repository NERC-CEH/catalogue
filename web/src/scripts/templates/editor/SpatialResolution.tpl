<div class="row">
  <div class="col-sm-2">
    <label class="control-label" for="spatialResolution<%= data.index %>EquivalentScale">Equivalent Scale</label>
  </div>
  <div class="col-sm-3">
    <input data-name='equivalentScale' type="number" class="editor-input" id="spatialResolution<%= data.index %>EquivalentScale" value="<%= data.equivalentScale %>">
  </div>
  <div class="col-sm-1 text-right"><i>OR</i></div>
  <div class="col-sm-2 col-sm-offset-1">
    <label class="control-label" for="spatialResolution<%= data.index %>Distance">Distance (metres)</label>
  </div>
  <div class="col-sm-3">
    <input data-name='distance' type="number" step="1" min="1" class="editor-input" id="spatialResolution<%= data.index %>Distance" value="<%= data.distance %>">
  </div>
</div>