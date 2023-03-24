<% /*
 This underscore template generates a given facet results client side. 
 It is recusively defined and as such needs to be supplied with itself

 IMPORTANT: If you change the structure of this, please update the 
 corresponding freemarker template /templates/search/_facets.tpl
*/ %>
<ul>
  <% _.each(results, function(facet) { %>
    <li>
      <a href="<%=facet.url%>" title="<%=facet.name%> (<%=facet.count%>)"><%=facet.name%> <small class="facet-count">(<%=facet.count%>)</small></a>

      <% if(facet.active) { %>
        <a href="<%=facet.url%>" title="<%=facet.name%> (<%=facet.count%>)">
          <span class="fa-solid fa-times"></span>
        </a>
      <% } %>

      <% if (facet.subFacetResults) { %>
        <%= template({results:facet.subFacetResults, template: template}) %>
      <% } %>
    </li>
  <% }); %>
</ul>