import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-xl-1">
        <label for="<%= data.modelAttribute %><%= data.index %>Code">Code</label>
    </div>
    <div class="col-xl-11">
        <select data-name="code" id="<%= data.modelAttribute %><%= data.index %>Code" class="form-select">
            <option class="option" value="" selected >- Select Type -</option>
            <option class="option" value="copyright">Copyright</option>
            <option class="option" value="intellectualPropertyRights">Intellectual Property Rights</option>
            <option class="option" value="license">License</option>
            <option class="option" value="otherRestrictions">Other Restrictions</option>
            <option class="option" value="patent">Patent</option>
            <option class="option" value="patentPending">Patent Pending</option>
            <option class="option" value="restricted">Restricted</option>
            <option class="option" value="trademark">Trademark</option>
        </select>
    </div>
</div>
<div class="row">
    <div class="col-xl-1">
        <label for="<%= data.modelAttribute %><%= data.index %>Value">Text</label>
    </div>
    <div class="col-xl-11">
      <textarea data-name="value" id="<%= data.modelAttribute %><%= data.index %>Value" class="editor-textarea"><%= data.value %></textarea>
    </div>
</div>
<div class="row">
    <div class="col-xl-1">
        <label for="<%= data.modelAttribute %><%= data.index %>Uri">URL</label>
    </div>
    <div class="col-xl-11">
        <input data-name="uri" id="<%= data.modelAttribute %><%= data.index %>Uri" class="editor-input" value="<%= data.uri %>">
    </div>
</div>
`)
