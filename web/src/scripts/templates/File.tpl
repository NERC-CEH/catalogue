<div id='<%= id %>' class='file' data-filename="<%= path %>">
    <div class='filename'>
        <i class='far fa-file-alt'></i> <span class='filename-label'><%= name %></span>
    </div>
    <% if (type != 'VALID') { %>
        <div class="invalid-container">
            </div><i class="fas fa-exclamation-triangle"></i> <%= type %></div>
        </div>
    <% } %>
</div>