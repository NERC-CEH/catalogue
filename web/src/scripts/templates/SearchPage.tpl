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
  <li class="previous"><a href="#">&larr; Older</a></li>
  <li>Page 1</li>
  <li class="next"><a href="#">Next &rarr;</a></li>
</ul>