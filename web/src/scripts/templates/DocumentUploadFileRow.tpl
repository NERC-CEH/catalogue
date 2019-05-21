<div class="row file-row is-<%= type %>" title="<%= name %>">
    <div class="col-md-8">
        <div class="row">
            <div class="col-md-4 file-name">
                <% if (errorType === 'file') { %>
                    <span class="file-danger"><i class="file-icon fas fa-exclamation-circle"></i><%= name %></span>
                <% } else { %>
                    <span><%= name %></span>
                <% } %>
            </div>
            <div class="col-md-2 file-size"><%= size %></div>
            <div class="col-md-6 file-checksum">
                <% if (errorType === 'hash') { %>
                    <span class="file-danger"><i class="file-icon fas fa-exclamation-circle"></i><%= hash %></span>
                <% } else { %>
                    <span><%= hash %></span>
                <% } %>
            </div>
            <div class="col-md-12 file-message">
                <% if (typeof message !== 'undefined') { %>
                    <span><%= message %></span>
                <% } %>
            </div>
        </div>
    </div>
    <div class="col-md-4 file-actions">
        <% if (moving) { %>
            <button disabled class="file-action btn btn-success">
                <i class="btn-icon fas fa-ban"></i>
                <span>MOVING</span>
            </button>
        <% } else if (action === 'move-datastore') { %>
            <button class="move-datastore file-action btn btn-success" data-filename="<%= path %>">
                <i class="btn-icon fas fa-level-down-alt"></i>
                <span>MOVE DATASTORE</span>
            </button>
        <% } else if (action === 'move-metadata') { %>
            <button class="move-metadata file-action btn btn-success" data-filename="<%= path %>">
                <i class="btn-icon fas fa-level-up-alt"></i>
                <span>MOVE METADATA</span>
            </button>
        <% } else if (action === 'move-both') { %>
            <button class="delete file-action btn btn-danger" data-filename="<%= path %>">
                <i class="btn-icon fas fa-trash"></i>
                <span>DELETE</span>
            </button>
            <button class="move-both move-metadata file-action btn btn-success" data-filename="<%= path %>">
                <i class="btn-icon fas fa-level-down-alt"></i>
                <span>MOVE METADATA</span>
            </button>
            <button class="move-both move-datastore file-action btn btn-success" data-filename="<%= path %>">
                <i class="btn-icon fas fa-level-down-alt"></i>
                <span>MOVE DATASTORE</span>
            </button>
        <% } else if (action === 'validate') { %>
            <button class="validate file-action btn btn-primary" data-filename="<%= path %>">
                <i class="btn-icon fas fa-check"></i>
                <span>VALIDATE</span>
            </button>
        <% } else if (action === 'accept') { %>
            <button class="delete file-action btn btn-danger" data-filename="<%= path %>">
                <i class="btn-icon fas fa-trash"></i>
                <span>DELETE</span>
            </button>
            <button class="accept file-action btn btn-primary" data-filename="<%= path %>">
                <i class="btn-icon fas fa-check"></i>
                <span>ACCEPT</span>
            </button>
        <% } else if (action === 'ignore') { %>
            <button class="ignore file-action btn btn-danger" data-filename="<%= path %>">
                <i class="btn-icon fas fa-times"></i>
                <span>IGNORE</span>
            </button>
        <% } %>
    </div>
</div>