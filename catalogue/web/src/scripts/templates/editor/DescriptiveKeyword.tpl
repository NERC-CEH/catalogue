<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label">Type</label>
  </div>
  <div class="col-sm-11 col-lg-11">
    <div class="btn-group btn-group-sm descriptiveKeywordType">
      <button type="button" class="btn btn-default">Discipline</button>
      <button type="button" class="btn btn-default">Place</button>
      <button type="button" class="btn btn-default">Stratum</button>
      <button type="button" class="btn btn-default">Temporal</button>
      <button type="button" class="btn btn-default">Theme</button>
    </div>
  </div>
</div>
<h5>Keywords</h5>
<div class="keywords"></div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <h5>Thesaurus</h5>
  </div>
  <div class="col-sm-1 col-lg-1 col-sm-offset-9 col-lg-offset-9">
    <button class="editor-button add">Add <span class="glyphicon glyphicon-plus" aria-hidden="true"></span></button>
  </div>
</div>
<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="descriptiveKeyword<%= data.index %>title">Title</label>
  </div>
  <div class="col-sm-11 col-lg-11">
      <input data-name='title' class="editor-input" id="descriptiveKeyword<%= data.index %>title" value="<%= data.thesaurusName.title %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label class="control-label" for="descriptiveKeyword<%= data.index %>Date">Date</label>
  </div>
  <div class="col-sm-2 col-lg-2">
    <input data-name='date' class="editor-input date" id="descriptiveKeyword<%= data.index %>Date" value="<%= data.thesaurusName.date %>">
  </div>
  <div class="col-sm-2 col-lg-2">
    <label class="control-label">Date Type</label>
  </div>
  <div class="col-sm-7 col-lg-7">
    <div class="btn-group btn-group-sm dateType">
      <button type="button" class="btn btn-default">Creation</button>
      <button type="button" class="btn btn-default">Publication</button>
      <button type="button" class="btn btn-default">Revision</button>
    </div>
  </div>
</div>