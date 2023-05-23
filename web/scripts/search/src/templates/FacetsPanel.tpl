<% /*
 This underscore template generates a facet results set with a heading

 IMPORTANT: If you change the structure of this, please update the 
 corresponding freemarker template /templates/search/_facets.tpl
*/ %>
<% _.each(facets, function(facet) { %>
  <h3><%= facet.displayName %></h3>
  <%= template({"results": facet.results, "template": template}) %>
<% }); %>
