import _ from 'underscore'

/*
 * This underscore template generates a given facet results client side.
 * It is recursively defined and as such needs to be supplied with itself
 *
 * IMPORTANT: If you change the structure of this, please update the
 * corresponding freemarker template /templates/search/_facets.ftlh
 */
export default _.template(`
    <select style="display: none;" class="form-select search-facet" multiple="multiple">
        <% _.each(facet.results, function(facetItem) { %>
            <% if(facetItem.name != 'Unknown') { %>
                <option <% if(facetItem.active) { %> selected="selected" <% } %> value="<%=facetItem.url%>"><%=facetItem.name%></option>
            <% } %>
        <% }); %>
    </select>
`)
