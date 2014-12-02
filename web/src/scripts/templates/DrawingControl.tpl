<% /*
 This underscore template generates the drawing control view client side.

 IMPORTANT: If you change the structure of this, please update the 
 corresponding freemarker template /templates/search/_drawing.html.tpl
*/ %>
<% if(removeBbox) { %>
  <a href="<%=removeBbox%>" class="btn btn-primary btn-xs">
    Spatial Filter 
    <span class="glyphicon glyphicon-remove"></span>
  </a>
<% } else { %>
  <button type="button" class="btn btn-default btn-xs">
    <span class="glyphicon glyphicon-pencil"></span>
  </button>
<% } %>