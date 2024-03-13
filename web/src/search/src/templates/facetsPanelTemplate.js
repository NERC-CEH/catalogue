import _ from 'underscore'

/*
 * This underscore template generates a facet results set with a heading
 *
 * IMPORTANT: If you change the structure of this, please update the
 * corresponding freemarker template /templates/search/_facets.tpl
 */
export default _.template(`
<% _.each(facets, function(facet) { %>
  <div class="facet">
    <div class="facet-header"><%= facet.displayName %></div>
    <%= template({"results": facet.results, "template": template}) %>
  </div>
<% }); %>
`)
