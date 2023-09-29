import _ from 'underscore'

export default _.template(`
<select data-name="<%= data.modelAttribute %>" class="editor-input" id="input-<%= data.modelAttribute %>"></select>
`)
