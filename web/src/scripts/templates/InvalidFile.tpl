<div id='<%= id %>' class='file file-invalid' data-filename="<%= path %>">
    <div class='filename'>
        <i class='far fa-file-alt'></i> <span class='filename-label'><%= name %></span>
    </div>
    <div class="invalid-container">
        <div class="pull-right">
            <% if (type === 'INVALID_HASH' || type == 'UNKNOWN_FILE') { %>
                <button class="btn btn-xs btn-success accept">Accept</button>
            <% } %>
            <% if (type == 'MISSING_FILE') { %>
                <button class="btn btn-xs btn-danger ignore">Ignore</button>
            <% } %>
            <% if (type == 'UNKNOWN_FILE') { %>
                <button class="btn btn-xs btn-danger delete" data-toggle="modal" data-target="#documentUploadModal">Delete</button>
            <% } %>
        </div>
        </div><i class="fas fa-exclamation-triangle"></i> <%= comment %></div>
    </div>
</div>