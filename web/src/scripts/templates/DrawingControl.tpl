<% /*
 This underscore template generates the drawing control view client side.

 IMPORTANT: If you change the structure of this, please update the 
 corresponding freemarker template /templates/search/_drawing.html.tpl
*/ %>
<% if(removeBbox) { %>
  <div class="dropdown" style="display:inline-block;">
    <button class="btn btn-default btn-xs dropdown-toggle" id="spatial-op" type="button" data-toggle="dropdown" aria-expanded="true">
      <%=spatialOp%>
      <span class="caret"></span>
    </button>
    <ul class="dropdown-menu input-xs" role="menu" aria-labelledby="spatial-op">
      <li role="presentation"><a role="menuitem" tabindex="-1" href="<%=iswithin%>">Entirely Within</a></li>
      <li role="presentation"><a role="menuitem" tabindex="-1" href="<%=intersects%>">Intersecting</a></li>
    </ul>
  </div>

  <a href="<%=removeBbox%>" class="btn btn-primary btn-xs">
    Spatial Filter 
    <span class="glyphicon glyphicon-remove"></span>
  </a>
<% } else { %>
  <button type="button" class="btn btn-default btn-xs">
    <span class="glyphicon glyphicon-pencil"></span>
  </button>
<% } %>