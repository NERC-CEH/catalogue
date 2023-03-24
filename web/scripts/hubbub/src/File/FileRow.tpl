<div class="panel panel-file <%= classes %>">
    <div class="panel-heading">
        <% if (errorType === 'file') { %>
             <i class="file-icon fa-solid fa-exclamation-circle"></i>
         <% } else { %>
            <% if (status === 'MOVING_FROM' || status === 'MOVING_TO') { %>
                <span class="panel-heading-type"><%= size %></span>
            <% } %>
        <% } %>
        <span><strong><%= status %></strong>&nbsp;</span>
        <span>&nbsp;<%= path %></span>

        <i class="panel-heading-chevron fa-solid fa-chevron-down"></i>
    </div>
    <div class="panel-body">
        <p>
            <b>Path: </b> <span><%= path %></span>
        </p>
        <div class="row">
            <div class="col-md-2"><b>Status</b></div>
            <div class="col-md-2"><b>Size</b></div>
            <div class="col-md-5"><b>Checksum</b></div>
            <div class="col-md-3"><b>Last validated</b></div>
        </div>
        <div class="row">
            <div class="col-md-2"><%= status %></div>
            <div class="col-md-2"><%= size %></div>
            <div class="col-md-5"><%= hash %></div>
            <div class="col-md-3"><%= date %></div>
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
            <% if (datastore === 'dropbox' && moving) { %>
                <button class="cancel btn btn-success">
                    <i class="btn-icon fa-solid fa-ban"></i>
                    <span>Cancel</span>
                </button>
            <% } else if (action === 'move-datastore') { %>
                <button class="move-datastore btn btn-success">
                    <i class="btn-icon fa-solid fa-level-down-alt"></i>
                    <span>Move to datastore</span>
                </button>
            <% } else if (action === 'move-metadata') { %>
                <button class="move-metadata btn btn-success">
                    <i class="btn-icon fa-solid fa-level-up-alt"></i>
                    <span>Move to metadata</span>
                </button>
            <% } else if (action === 'move-both') { %>
                <button class="delete btn btn-danger">
                    <i class="btn-icon fa-solid fa-trash"></i>
                    <span>Delete</span>
                </button>
                <button class="move-both move-metadata btn btn-success">
                    <i class="btn-icon fa-solid fa-level-down-alt"></i>
                    <span>Move to metadata</span>
                </button>
                <button class="move-both move-datastore btn btn-success">
                    <i class="btn-icon fa-solid fa-level-down-alt"></i>
                    <span>Move to datastore</span>
                </button>
            <% } else if (datastore === 'dropbox' && action === 'accept') { %>
                <button class="accept btn btn-primary">
                    <i class="btn-icon fa-solid fa-check"></i>
                    <span>Accept</span>
                </button>
            <% } else if (datastore === 'dropbox' && action === 'ignore') { %>
                <button class="ignore btn btn-danger">
                    <i class="btn-icon fa-solid fa-times"></i>
                    <span>Ignore</span>
                </button>
            <% }
            if (datastore === 'dropbox') { %>
                <button class="delete btn btn-danger">
                    <i class="btn-icon fa-solid fa-trash"></i>
                    <span>Delete</span>
                </button>
            <% } %>
            <button class="validate btn btn-primary">
                <i class="btn-icon fa-solid fa-check"></i>
                <span>Validate</span>
            </button>
        </div>
    </div>
</div>
