import _ from 'underscore'

export default _.template(`
<span data-name="<%= data.modelAttribute %>" id="input-<%= data.modelAttribute %>" class="readonly form-control-static"><%= data.value %> </span>
`)
