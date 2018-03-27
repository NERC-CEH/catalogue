<div class='message'>
    <% if (type === "loading") { %>
        <span class="fas fa-sync fa-spin"></span>
    <% } else if (type === "warning") { %>
        <span class="fas fa-exclamation-triangle"></span>
    <% } else if (type === "info") { %>
        <span class="fas fa-info"></span>
    <% } else if (type === "success") { %>
        <span class="fas fa-check"></span>
    <% } %>
    <span><%= message %></span>
    <a class="close">×</a>
</div>