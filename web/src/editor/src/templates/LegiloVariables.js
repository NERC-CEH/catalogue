import baseTemplate from './LegiloBase'

const tableTemplate = `
  <thead>
    <tr>
      <th scope="col" class="col-1">Select</th>
      <th scope="col">Name</th>
      <th scope="col">Title</th>
      <th scope="col">Unit</th>
      <th scope="col">Description</th>
      <th scope="col">Confidence</th>
    </tr>
  </thead>
  <tbody class="suggestions-table-body table-group-divider">
    <% if (suggestions && suggestions.length > 0) { %>
      <% _.each(suggestions, function(suggestion) { %>
        <tr>
          <td><input type="checkbox" class="suggestions-checkbox" data-value="<%= suggestion.name || '' %>" data-title="<%= suggestion.longName || '' %>" data-units="<%= suggestion.units || '' %>" data-description="<%= suggestion.meaning || '' %>" <% if (suggestion.isChecked) { %>checked<% } %>></td>
          <td><%= suggestion.name || '' %></td>
          <td><%= suggestion.longName || '' %></td>
          <td><%= suggestion.units || '' %></td>
          <td><%= suggestion.meaning || '' %></td>
          <td><%= suggestion.confidence || 0 %></td>
        </tr>
      <% }); %>
    <% } %>
  </tbody>
`

export default baseTemplate('Suggested Variables', tableTemplate)
