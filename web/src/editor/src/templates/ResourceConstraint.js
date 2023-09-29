import _ from 'underscore'

export default _.template(`
<div class="row">
  <div class="col-sm-1">
    <label for="<%= data.modelAttribute %><%= data.index %>Code">Code</label>
  </div>
  <div class="col-sm-11">
    <select data-name="code" class="editor-input" id="<%= data.modelAttribute %><%= data.index %>Code">
      <option value="" selected >- Select Type -</option>
      <option value="copyright">Copyright</option>
      <option value="intellectualPropertyRights">Intellectual Property Rights</option>
      <option value="license">License</option>
      <option value="otherRestrictions">Other Restrictions</option>
      <option value="patent">Patent</option>
      <option value="patentPending">Patent Pending</option>
      <option value="restricted">Restricted</option>
      <option value="trademark">Trademark</option>
    </select>
  </div>
</div>
<div class="row">
  <div class="col-sm-1">
    <label for="<%= data.modelAttribute %><%= data.index %>Value">Text</label>
  </div>
  <div class="col-sm-11">
    <input data-name="value" id="<%= data.modelAttribute %><%= data.index %>Value" class="editor-input" value="<%= data.value %>">
  </div>
</div>
<div class="row">
  <div class="col-sm-1">
    <label for="<%= data.modelAttribute %><%= data.index %>Uri">URL</label>
  </div>
  <div class="col-sm-11">
    <input data-name="uri" id="<%= data.modelAttribute %><%= data.index %>Uri" class="editor-input" value="<%= data.uri %>">
  </div>
</div>
`)
