<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label for="qa<%= data.index %>Done">Done</label>
  </div>
  <div class="col-sm-2 col-lg-2">
    <select data-name="done" id="qa<%= data.index %>Done" class="editor-input" value="<%= data.done %>">
      <option value="unknown">Unknown</option>
      <option value="yes">Yes</option>
      <option value="no">No</option>
    </select>
  </div>
  <div class="col-sm-1 col-lg-1">
    <label for="qa<%= data.index %>ModelVersion">Model version</label>
  </div>
  <div class="col-sm-2 col-lg-2">
    <input data-name="modelVersion" id="qa<%= data.index %>ModelVersion" class="editor-input" value="<%= data.modelVersion %>">
  </div>
  <div class="col-sm-1 col-lg-1">
    <label for="qa<%= data.index %>Owner">Owner</label>
  </div>
  <div class="col-sm-2 col-lg-2">
    <input data-name="owner" id="qa<%= data.index %>Owner" class="editor-input" value="<%= data.owner %>">
  </div>
  <div class="col-sm-1 col-lg-1">
    <label for="qa<%= data.index %>Date">Date</label>
  </div>
  <div class="col-sm-2 col-lg-2">
    <input data-name="date" id="qa<%= data.index %>Date" class="editor-input" value="<%= data.date %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label for="qa<%= data.index %>Note">Note</label>
  </div>
  <div class="col-sm-11 col-lg-11">
    <textarea data-name="note" rows="5" id="qa<%= data.index %>Note" class="editor-textarea"><%= data.note %></textarea>
  </div>
</div>