import _ from 'underscore'

export default _.template(`
<div class="row">
    <div class="col-lg-3 col-sm-5">
        <input data-name="key" type="text" id="qa<%= data.index %>Key" class="editor-input" value="<%= data.key %>" placeholder="Key (attribute name)">
    </div>
    <div class="col-lg-9 col-sm-7">
      <input data-name="value" type="text" id="qa<%= data.index %>Value" class="editor-input" value="<%= data.value %>" placeholder="Value">
    </div>
</div>
`)
