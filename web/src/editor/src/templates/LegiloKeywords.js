import baseTemplate from './LegiloBase'

const tableTemplate = `
  <thead>
    <tr>
      <th scope="col" class="col-1">Select</th>
      <th scope="col">Term</th>
      <th scope="col">URI</th>
      <th scope="col">Confidence</th>
    </tr>
  </thead>
  <tbody class="suggestions-table-body table-group-divider">
    <% if (suggestions && suggestions.length > 0) { %>
      <% _.each(suggestions, function(suggestion) { %>
        <tr>
          <td><input type="checkbox" class="suggestions-checkbox" data-term="<%= suggestion.name || '' %>" data-uri="<%= suggestion.uri || '' %>"  <% if (suggestion.isChecked) { %>checked<% } %>></td>
          <td><%= suggestion.name || '' %></td>
          <td><%= suggestion.uri || '' %></td>
          <td><%= suggestion.confidence || 0 %></td>
        </tr>
      <% }); %>
    <% } %>
  </tbody>
`

export default baseTemplate('Suggested Keywords', tableTemplate)
