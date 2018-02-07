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
    <h2 class="resultTitle">
      <small>
      <span><%=result.resourceType%></span>
      <% if(result.resourceStatus != '' && result.resourceStatus != 'Current') {  %>
        <span class="label-<%=result.resourceStatus%>">(<%=result.resourceStatus%>)</span>
      <% } %>
      <% if(result.state == 'draft') { %>
        <span class="text-danger"><b>DRAFT</b></span>
      <% } else if(result.state == 'pending') { %>
        <span class="text-danger"><b>PENDING PUBLICATION</b></span>
      <% } %>
      </small><br>
      <a href="/documents/<%=result.identifier%>" class="title"><%=result.title%></a>
    </h2>
    <div class="resultDescription"><%=result.shortenedDescription%></div>
  </div>
<% }); %>
<ul class="pager">
  <% if(prevPage) { %>
    <li class="previous"><a href="<%=prevPage%>">&larr; Previous</a></li>
  <% } %>
  <li>Page <%=page%></li>          
  <% if(nextPage) { %>
    <li class="next"><a href="<%=nextPage%>">Next &rarr;</a></li>
  <% } %>
</ul>
