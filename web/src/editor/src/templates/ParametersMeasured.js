import _ from 'underscore'

export default _.template(`
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="parametersMeasured<%= data.index %>NameTitle">Name: title</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name="name.title" id="parametersMeasured<%= data.index %>NameTitle" class="editor-input" value="<%= data.name.title %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="parametersMeasured<%= data.index %>NameHref">Name: url</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name="name.href" id="parametersMeasured<%= data.index %>NameHref" class="editor-input" value="<%= data.name.href %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="parametersMeasured<%= data.index %>UnitOfMeasureTitle">Unit of Measure: title</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name="unitOfMeasure.title" id="parametersMeasured<%= data.index %>UnitOfMeasureTitle" class="editor-input" value="<%= data.unitOfMeasure.title %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-2 col-lg-2">
    <label for="parametersMeasured<%= data.index %>UnitOfMeasureHref">Unit of Measure: url</label>
  </div>
  <div class="col-sm-10 col-lg-10">
    <input data-name="unitOfMeasure.href" id="parametersMeasured<%= data.index %>UnitOfMeasureHrefe" class="editor-input" value="<%= data.unitOfMeasure.href %>">
  </div>
</div>
`)
