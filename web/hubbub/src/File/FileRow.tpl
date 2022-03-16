<div class="panel panel-file <%= classes %>">
    <div class="panel-heading">
        <% if (errorType === 'file') { %>
            <span class="panel-heading-filename">
                <i class="file-icon fas fa-exclamation-circle"></i>
                <%= path %>
            </span>
            <span class="panel-heading-type"><%= status %></span>
        <% } else if (status !== 'VALID') { %>
            <span class="panel-heading-filename"><%= path %></span>
            <span class="panel-heading-type"><%= status %></span>
            <% if (status === 'MOVING_FROM' || status === 'MOVING_TO' || status === 'WRITING') { %>
            <span class="panel-heading-type"><%= size %></span> <div><i class="fas fa-sync fa-spin"></i></div>
            <% } %>
        <% } else { %>
            <span class="panel-heading-filename"><%= path %></span>
        <% } %>
        <i class="panel-heading-chevron fas fa-chevron-down"></i>
    </div>
    <div class="panel-body">
        <p>
            <b>Path</b><br/>
            <span><%= path %></span>
        </p>
        <div class="row">
            <div class="col-md-2"><b>Type</b></div>
            <div class="col-md-2"><b>Size</b></div>
            <div class="col-md-3"><b>Checksum</b></div>
            <div class="col-md-2"><b>Last validated</b></div>
            <div class="col-md-3"><b>Validation estimate</b></div>
        </div>
        <div class="row">
            <div class="col-md-2"><%= status %></div>
            <div class="col-md-2"><%= size %></div>
            <div class="col-md-3"><%= hash %></div>
            <div class="col-md-2"><%= date %></div>
            <div class="col-md-3"><%= estimate %></div>
        </div>

       <% if (message) { %>
        <div class="panel-file-message">
            <% if (message.title) { %>
            <span class="panel-file-message-title"><%= message.title %></span>
            <% } %>
            <p class="panel-file-message-body"><%= message.content %></p>
        </div>
        <% } %>
    </div>
    <div class="panel-footer">
        <div class="buttons">
            <% if (moving) { %>
                <button class="cancel btn btn-success">
                    <i class="btn-icon fas fa-ban"></i>
                    <span>Cancel</span>
                </button>
            <% } else if (action === 'move-datastore') { %>
                <button class="move-datastore btn btn-success">
                    <i class="btn-icon fas fa-level-down-alt"></i>
                    <span>Move to Datastore</span>
                </button>
            <% } else if (action === 'move-metadata') { %>
                <button class="move-metadata btn btn-success">
                    <i class="btn-icon fas fa-level-up-alt"></i>
                    <span>Move to Metadata</span>
                </button>
            <% } else if (action === 'move-both') { %>
                <button class="delete btn btn-danger">
                    <i class="btn-icon fas fa-trash"></i>
                    <span>Delete</span>
                </button>
                <button class="move-both move-metadata btn btn-success" data-filename="<%= path %>">
                    <i class="btn-icon fas fa-level-down-alt"></i>
                    <span>Move to Metadata</span>
                </button>
                <button class="move-both move-datastore btn btn-success">
                    <i class="btn-icon fas fa-level-down-alt"></i>
                    <span>Move to Datastore</span>
                </button>
            <% } else if (action === 'accept') { %>
                <button class="delete btn btn-danger">
                    <i class="btn-icon fas fa-trash"></i>
                    <span>Delete</span>
                </button>
                <button class="accept btn btn-primary">
                    <i class="btn-icon fas fa-check"></i>
                    <span>Accept</span>
                </button>
            <% } else if (action === 'ignore') { %>
                <button class="ignore btn btn-danger">
                    <i class="btn-icon fas fa-times"></i>
                    <span>Ignore</span>
                </button>
            <% } %>
            <button class="validate btn btn-primary">
                <i class="btn-icon fas fa-check"></i>
                <span>Validate</span>
            </button>
        </div>
    </div>
</div>
