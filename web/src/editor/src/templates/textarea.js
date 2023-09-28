import _ from 'underscore'

export default _.template(`
<textarea data-name="<%= data.modelAttribute %>" rows="<%= data.rows %>" placeholder="<%= data.placeholderAttribute %>" class="editor-textarea" id="input-<%= data.modelAttribute %>"><%= data.value %></textarea>
`)
