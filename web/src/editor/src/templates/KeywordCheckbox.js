import _ from 'underscore'

export default _.template(`
<label>
  <input type="checkbox" <% if (toSearch) { %> checked <% } %>>
  <span><%= name %></span>
</label>
`)
