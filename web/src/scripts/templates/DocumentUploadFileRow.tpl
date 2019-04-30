<div class="row file-row">
    <div class="col-md-8">
        <div class="row">
            <div class="col-md-4 file-name">
                <% if (errorType === 'file') { %>
                    <span class="file-danger"><i class="file-icon fas fa-exclamation-circle"></i><%= name %></span>
                <% } else { %>
                    <span><%= name %></span>
                <% } %>
            </div>
            <div class="col-md-2 file-size"><%= bytes %> B</div>
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
        <button class="file-action btn btn-success">
            <i class="btn-icon fas fa-level-down-alt"></i>
            <span>MOVE DATASTORE</span>
        </button>
    </div>
</div>