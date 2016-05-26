<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label class="control-label" for="conformanceResult<%= data.index %>Title">Title</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name="title" id="conformanceResult<%= data.index %>Title" class="editor-input" value="<%= data.title %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label class="control-label" for="conformanceResult<%= data.index %>Date">Date</label>
  </div>
  <div class="col-sm-2 col-lg-2">
    <input data-name='date' class="editor-input date" id="conformanceResult<%= data.index %>Date" value="<%= data.date %>">
  </div>
  <div class="col-sm-2 col-lg-2">
    <label class="control-label" for="conformanceResult<%= data.index %>dateType">Date Type</label>
  </div>
  <div class="col-sm-3 col-lg-3">
    <select data-name="dateType" class="editor-input dateType" id="conformanceResult<%= data.index %>dateType">
      <option value="" selected >- Select Date Type -</option>
      <option value="creation">Creation</option>
      <option value="publication">Publication</option>
      <option value="revision">Revision</option>
    </select>
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label class="control-label">Pass</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <label class="radio-inline">
      <input data-name="pass" type="radio" name="pass<%= data.index %>" value="true"> True
    </label>
    <label class="radio-inline">
      <input data-name="pass" type="radio" name="pass<%= data.index %>" value="false"> False
    </label>
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label class="control-label" for="conformanceResult<%= data.index %>Explanation">Explanation</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <textarea data-name="explanation" rows="7" id="conformanceResult<%= data.index %>Explanation" class="editor-textarea"><%= data.explanation %></textarea>
  </div>
</div>
