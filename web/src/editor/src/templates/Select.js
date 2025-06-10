import _ from 'underscore'

export default _.template(`
<select data-name="<%= data.modelAttribute %>" id="input-<%= data.modelAttribute %>"></select>
`)
