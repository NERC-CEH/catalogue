import _ from 'underscore'

export default _.template(`
<div class="row">
  <input data-name="value" id="otherConstraint<%= data.index %>Value" class="editor-input" value="<%= data.value %>">
</div>
`)
