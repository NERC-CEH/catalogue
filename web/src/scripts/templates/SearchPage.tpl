<% /*
 This underscore template generates the search results client side.

 IMPORTANT: If you change the structure of this, please update the 
 corresponding freemarker template /templates/search/_page.tpl
*/ %>
<div class="search-results-heading">
  <span id="num-records"><%=numFound%></span> records found
</div>
<% _.each(results, function(result) { %>
  <div class="result" id="<%=result.identifier%>">
    <h2>
      <a href="/documents/<%=result.identifier%>" class="title"><%=result.title%></a>
    </h2>
    <div class="description"><%=result.shortenedDescription%></div>
  </div>
<% }); %>
<ul class="pager">
  <% if(prevPage) { %>
    <li class="previous"><a href="<%=prevPage%>">&larr; Older</a></li>
  <% } %>
  <li>Page <%=page%></li>          
  <% if(nextPage) { %>
    <li class="next"><a href="<%=nextPage%>">Next &rarr;</a></li>
  <% } %>
</ul>