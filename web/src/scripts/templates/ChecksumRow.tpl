<tr data-file="<%= filename %>">
    <td class='checksum-file'><%= filename %></td>
    <td class='checksum-value'><%= md5Hash %></td>
    <% if (canDelete) { %>
        <td class="checksum-delete text-center">
            <button class="btn btn-block btn-danger delete" data-file="<%= filename %>">
                <i class="fa fa-trash-o"></i> Delete
            </button>
        </td>
    <% } %>
    <% if (canChangeType) { %>
        <td>
            <form action="">
                <div class="change-type">
                    <input type="radio" id="to-data-${filename}" name="type" value="data" <%= isData %> />
                    <label for="to-data-${filename}">Data</label>
                </div>
                <div class="change-type">
                    <input type="radio" id="to-meta-${filename}" name="type" value="meta" <%= isMeta %> />
                    <label for="to-meta-${filename}">Meta</label>
                </div>
            </form>
        </td>
    <% } %>
</tr>