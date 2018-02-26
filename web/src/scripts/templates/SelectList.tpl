<li class='delete-parent'>
    <div class="value-block">
        <a id="" href="#" class="delete">
            <i class="fa fa-times"></i>
        </a>
        <div class="value-block-value">
            <select name="<%= name %>[<%= index %>]">
                <% for (var i = 0; i < options.length; i++) { %>
                    <option <% if (options[i].selected) { %>selected<% } %> value="<%= options[i].value %>"><%= options[i].label %></option>                    
                <% } %>
                <option value="other">Other</option>
            </select>
            <% if (hasLink) { %>
            <div>
                <a class="static-value" href="/documents/<%= id %>"><%= title %></a>
            </div>
            <% } %>
        </div>
    </div>
</li>