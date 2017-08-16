<tr data-file="<%= filename %>">
    <td><%= md5Hash %></td>
    <td><%= filename %></td>
    <% if (canDelete) { %>
        <td class="text-center">
            <button class="btn btn-block btn-danger delete" data-file="<%= filename %>">
                <i class="glyphicon glyphicon-trash"></i> Delete
            </button>
        </td>
    <% } %>
</tr>