<% /*
 This underscore template generates the drawing control view client side.

 IMPORTANT: If you change the structure of this, please update the 
 corresponding freemarker template /templates/search/_drawing.ftl
*/ %>
<% if(withoutBbox) { %>
  <div class="btn-group">
    <button id="spatial-op-dropdown"  class="btn btn-default btn-xs dropdown-toggle" type="button" data-toggle="dropdown">
      <% if (withinBbox) { %>
        Overlapping
      <% } else { %>
        Entirely Within
      <% } %>
      <span class="caret"></span>
    </button>
    <ul class="dropdown-menu" role="menu">
      <li role="presentation"><a role="menuitem" tabindex="-1" href="<%= withinBbox || url %>">Entirely Within</a></li>
      <li role="presentation"><a role="menuitem" tabindex="-1" href="<%= intersectingBbox || url %>">Overlapping</a></li>
    </ul>
  </div>

  <a href="<%=withoutBbox%>" class="btn btn-primary btn-xs">
    Spatial Filter 
    <span class="fas fa-times"></span>
  </a>
<% } else { %>
  <button id="drawing-toggle" type="button" class="btn btn-default btn-xs">
    <span class="fas fa-pencil-alt"></span>
  </button>
<% } %>