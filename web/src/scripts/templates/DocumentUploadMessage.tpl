<div class='message'>
    <% if (type === "loading") { %>
        <span class="fa fa-refresh fa-spin"></span>
    <% } else if (type === "warning") { %>
        <span class="fa fa-exclamation-triangle"></span>
    <% } else if (type === "info") { %>
        <span class="fa fa-info"></span>
    <% } else if (type === "success") { %>
        <span class="fa fa-check"></span>
    <% } %>
    <span><%= message %></span>
    <a class="close">×</a>
</div>