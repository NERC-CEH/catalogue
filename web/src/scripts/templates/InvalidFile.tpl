<div id='<%= id %>' class='file file-invalid btn btn-primary'>
    <p class='filename'>
        <i class='fa fa-file-text-o'></i> <span class='filename-label'><%= name %></span>
    </p>
    <div class="invalid-container">
        <i class="fa fa-warning"></i> <span><%= comment %></span>
        <div class="text-right">
            <% if (type === 'INVALID_HASH' || type == 'UNKNOWN_FILE') { %>
                <button class="btn btn-success accept">Accept</button>
            <% } %>
            <% if (type == 'MISSING_FILE') { %>
                <button class="btn btn-danger ignore">Ignore</button>
            <% } %>
            <% if (type == 'UNKNOWN_FILE') { %>
                <button class="btn btn-danger delete" data-toggle="modal" data-target="#documentUploadModal">Delete</button>
            <% } %>
        </div>
    </div>
</div>