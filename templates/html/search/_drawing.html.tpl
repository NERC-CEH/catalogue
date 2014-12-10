<#--
  This freemarker fragment generates the static view of the drawing control.
  This will either be a button (for toggling drawing mode) or a anchor (for 
  removing a bbox filter)

  IMPORTANT: If you change the structure of this, please update the corresponding 
  javascript template /web/src/scripts/templates/DrawingControl.tpl
-->
<#if withoutBBox?has_content>
  <div class="dropdown" style="display:inline-block;">
    <button class="btn btn-default btn-xs dropdown-toggle" id="spatial-op" type="button" data-toggle="dropdown" aria-expanded="true">
      <#if overlappingBBox??>
        Intersecting
      <#else>
        Entirely Within
      </#if>
      <span class="caret"></span>
    </button>
    <ul class="dropdown-menu input-xs" role="menu" aria-labelledby="spatial-op">
      <li role="presentation"><a role="menuitem" tabindex="-1" href="${overlappingBBox!url}">Entirely Within</a></li>
      <li role="presentation"><a role="menuitem" tabindex="-1" href="${intersectingBBox!url}">Intersecting</a></li>
    </ul>
  </div>

  <a href="${withoutBBox}" class="btn btn-primary btn-xs">
    Spatial Filter 
    <span class="glyphicon glyphicon-remove"></span>
  </a>
<#else>
  <button type="button" class="btn btn-default btn-xs">
    <span class="glyphicon glyphicon-pencil"></span>
  </button>
</#if>