<tr class='invalid-row'>
    <td class='checksum-file'><%= name %></td>
    <td><%= hash %></td>
    <td><%= comments[comments.length - 1] %></td>
    <td class="text-center">
        <% if (type === "INVALID_HASH") { %>
            <button class="btn btn-block btn-success accept-invalid">
                <i class="fas fa-check"></i> Update checksum
            </button>
        <% } %>
        <% if (type === "UNKNOWN_FILE") { %>
            <button class="btn btn-block btn-success accept-invalid">
                <i class="fas fa-check"></i> Add file
            </button>
        <% } %>
    </td>
    <td class="text-center">
        <button class="btn btn-block btn-danger delete">
            <i class="fas fa-trash-alt"></i> Delete
        </button>
    </td>
</tr>