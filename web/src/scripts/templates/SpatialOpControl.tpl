<% /*
 This underscore template generates the Spatial Op control view client side.

 IMPORTANT: If you change the structure of this, please update the
 corresponding freemarker template /templates/search/_spatialOp.html.tpl
*/ %>
Show results
<div class="btn-group btn-group-sm">
  <button id="spatialOp" type="button" class="btn btn-default" data-iswithin-text="entirely within" data-intersects-text="that overlap">entirely within</button>
  <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
    <span class="caret"></span>
    <span class="sr-only">Toggle Dropdown</span>
  </button>
  <ul class="dropdown-menu" role="menu">
    <li><a href="#" data-op="iswithin">entirely within</a></li>
    <li><a href="#" data-op="intersects">that overlap</a></li>
  </ul>
</div>
Spatial Filter