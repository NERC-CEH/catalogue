import _ from 'underscore'

export default _.template(`
<input data-name="<%= data.modelAttribute %>" id="input-<%= data.modelAttribute %>" value="<%= data.value %>" type="checkbox" >
`)
