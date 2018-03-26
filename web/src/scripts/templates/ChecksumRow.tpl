<tr data-file="<%= filename %>">
    <td class='checksum-file'><%= filename %></td>
    <td class='checksum-value'><%= md5Hash %></td>
    <% if (canDelete) { %>
        <td class="checksum-delete text-center">
            <button class="btn btn-block btn-danger delete" data-toggle="modal" data-target="#documentUploadModal">
                <i class="fas fa-trash-alt"></i> Delete
            </button>
        </td>
    <% } %>
    <% if (canChangeType) { %>
    <td>
        <div class="btn-group data-meta-toggle" data-toggle="buttons">
            <% if (isData) { %>
                <button class='btn btn-success to-data is-initialising'>DATA</button>
                <button class="btn to-meta is-initialising">META</button>
            <% } else { %>
                <button class='btn to-data is-initialising'>DATA</button>
                <button class="btn btn-success to-meta is-initialising">META</button>
            <% } %>
        </div>
    </td>
    <% } %>
</tr>