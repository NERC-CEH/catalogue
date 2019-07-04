<% if (open && errorType !== 'valid') { %>
<div data-filename="<%= path %>" class="panel panel-danger panel-file">
<% } else if (open) { %>
<div data-filename="<%= path %>" class="panel panel-default panel-file">
<% } else if (errorType !== 'valid') { %>
<div data-filename="<%= path %>" class="panel panel-danger panel-file is-collapsed">
<% } else { %>
<div data-filename="<%= path %>" class="panel panel-default panel-file is-collapsed">
<% } %>    
    <div class="panel-heading">
        <% if (errorType === 'file') { %>
            <span class="panel-heading-filename">
                <i class="file-icon fas fa-exclamation-circle"></i>
                <%= name %>
            </span>
            <span class="panel-heading-type"><%= type %></span>
        <% } else if (type !== 'VALID') { %>
            <span class="panel-heading-filename"><%= name %></span>
            <span class="panel-heading-type"><%= type %></span>
            <% if (type === 'MOVING_FROM' || type === 'MOVING_TO' || type === 'WRITING') { %>
            <span class="panel-heading-type"><%= size %></span>
            <% } %>
        <% } else { %>
            <span class="panel-heading-filename"><%= name %></span>
        <% } %>
        <i class="panel-heading-chevron fas fa-chevron-down"></i>
    </div>
    <div class="panel-body">
        <p>
            <b>Path</b>
            <br />
            <span><%= path %></span>
        </p>
        <div class="row">
            <div class="col-md-2"><b>Type</b></div>
            <div class="col-md-2"><b>Size</b></div>
            <div class="col-md-3"><b>Checksum</b></div>
            <div class="col-md-2"><b>Last Changed</b></div>
            <div class="col-md-3"><b>Validation Estimate</b></div>
        </div>
        <div class="row">
            <div class="col-md-2"><%= type %></div>
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
                <button class="cancel btn btn-success" data-filename="<%= path %>">
                    <i class="btn-icon fas fa-ban"></i>
                    <span>CANCEL</span>
                </button>
            <% } else if (action === 'move-datastore') { %>
                <button class="move-datastore btn btn-success" data-filename="<%= path %>">
                    <i class="btn-icon fas fa-level-down-alt"></i>
                    <span>MOVE TO DATASTORE</span>
                </button>
            <% } else if (action === 'move-metadata') { %>
                <button class="move-metadata btn btn-success" data-filename="<%= path %>">
                    <i class="btn-icon fas fa-level-up-alt"></i>
                    <span>MOVE TO METADATA</span>
                </button>
            <% } else if (action === 'move-both') { %>
                <button class="delete btn btn-danger" data-filename="<%= path %>">
                    <i class="btn-icon fas fa-trash"></i>
                    <span>DELETE</span>
                </button>
                <button class="move-both move-metadata btn btn-success" data-filename="<%= path %>">
                    <i class="btn-icon fas fa-level-down-alt"></i>
                    <span>MOVE TO METADATA</span>
                </button>
                <button class="move-both move-datastore btn btn-success" data-filename="<%= path %>">
                    <i class="btn-icon fas fa-level-down-alt"></i>
                    <span>MOVE TO DATASTORE</span>
                </button>
            <% } else if (action === 'accept') { %>
                <button class="delete btn btn-danger" data-filename="<%= path %>">
                    <i class="btn-icon fas fa-trash"></i>
                    <span>DELETE</span>
                </button>
                <button class="accept btn btn-primary" data-filename="<%= path %>">
                    <i class="btn-icon fas fa-check"></i>
                    <span>ACCEPT</span>
                </button>
            <% } else if (action === 'ignore') { %>
                <button class="ignore btn btn-danger" data-filename="<%= path %>">
                    <i class="btn-icon fas fa-times"></i>
                    <span>IGNORE</span>
                </button>
            <% } %>
            <button class="validate btn btn-primary" data-filename="<%= path %>">
                <i class="btn-icon fas fa-check"></i>
                <span>VALIDATE</span>
            </button>
        </div>
    </div>
</div>