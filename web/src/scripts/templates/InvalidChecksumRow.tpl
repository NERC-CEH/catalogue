<tr class='invalid-row'>
    <td class='checksum-file'><%= name %></td>
    <td><%= hash %></td>
    <td><%= comments.join('<br />') %></td>
    <td class="text-center">
        <% if (type === "INVALID_HASH") { %>
            <button class="btn btn-block btn-success accept-invalid">
                <i class="fa fa-check"></i> Update checksum
            </button>
        <% } %>
        <% if (type === "NOT_META_OR_DATA") { %>
            <button class="btn btn-block btn-success accept-invalid">
                <i class="fa fa-check"></i> Add file
            </button>
        <% } %>
    </td>
    <td class="text-center">
        <button class="btn btn-block btn-danger delete">
            <i class="fa fa-trash-o"></i> Delete
        </button>
    </td>
</tr>