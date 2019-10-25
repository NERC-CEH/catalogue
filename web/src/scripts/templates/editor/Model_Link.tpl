<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label for="<%= data.modelAttribute %><%= data.index %>Title">Type</label>
  </div>
  <div class="col-sm-11 col-lg-11">
  <select data-name="title" id="<%= data.modelAttribute %><%= data.index %>Title" class="editor-input webtitle">
    <option value=""> -- Select --</option>
    <option value="repo">Code repository</option>
    <option value="documentation">Documentation</option>
    <option value="website">Website</option>
  </select>
    <!--<input  value="<%= data.title %>">-->
  </div>
</div>
<div class="row">
  <div class="col-sm-1 col-lg-1">
    <label for="<%= data.modelAttribute %><%= data.index %>URL">URL</label>
  </div>
  <div class="col-sm-11 col-lg-11">
    <input data-name="href" id="<%= data.modelAttribute %><%= data.index %>URL" class="editor-input" value="<%= data.href %>">
  </div>
</div>