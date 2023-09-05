<div class="row">
  <div class="col-sm-2">
    <label for="conformity<%= data.index %>Specification">Specification</label>
  </div>
  <div class="col-sm-6">
    <input data-name="specification" id="conformity<%= data.index %>Specification" class="editor-input" value="<%= data.specification %>">
  </div>
  <div class="col-sm-1">
    <label for="conformity<%= data.index %>Degree">Degree</label>
  </div>
  <div class="col-sm-3">
    <select data-name="degree" id="conformity<%= data.index %>Degree" class="editor-input">
      <option value="" selected >- Choose conformity -</option>
      <option value="notEvaluated">Not evaluated</option>
      <option value="pass">Conformant (pass)</option>
      <option value="fail">Non conformant (fail)</option>
    </select>
  </div>
</div>
<div class="row">
  <div class="col-sm-2">
    <label for="conformity<%= data.index %>Explanation">Explanation</label>
  </div>
  <div class="col-sm-10">
    <textarea rows="3" data-name="explanation" id="conformity<%= data.index %>Explanation" class="editor-textarea"><%= data.explanation %></textarea>
  </div>
</div>
<div class="row">

