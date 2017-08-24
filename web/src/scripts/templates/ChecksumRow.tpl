<tr data-file="<%= filename %>">
    <td class='checksum-value'><%= md5Hash %></td>
    <td class='checksum-file'><%= filename %></td>
    <% if (canDelete) { %>
        <td class="checksum-delete text-center">
            <button class="btn btn-block btn-danger delete" data-file="<%= filename %>">
                <i class="glyphicon glyphicon-trash"></i> Delete
            </button>
        </td>
    <% } %>
</tr>