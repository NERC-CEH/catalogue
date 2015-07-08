<#--
  This freemarker fragment generates the static view of the drawing control.
  This will either be a button (for toggling drawing mode) or a anchor (for 
  removing a bbox filter)

  IMPORTANT: If you change the structure of this, please update the corresponding 
  javascript template /web/src/scripts/templates/DrawingControl.tpl
-->
<#if withoutBbox?has_content>
  <div class="btn-group">
    <button id="spatial-op-dropdown" class="btn btn-default btn-xs dropdown-toggle" type="button" data-toggle="dropdown">
      <#if withinBbox?has_content>
        Overlapping
      <#else>
        Entirely Within
      </#if>
      <span class="caret"></span>
    </button>
    <ul class="dropdown-menu" role="menu">
      <li role="presentation"><a role="menuitem" tabindex="-1" href="${withinBbox!url}">Entirely Within</a></li>
      <li role="presentation"><a role="menuitem" tabindex="-1" href="${intersectingBbox!url}">Overlapping</a></li>
    </ul>
  </div>

  <a href="${withoutBbox}" class="btn btn-primary btn-xs">
    Spatial Filter 
    <span class="glyphicon glyphicon-remove"></span>
  </a>
<#else>
  <button id="drawing-toggle" type="button" class="btn btn-default btn-xs">
    <span class="glyphicon glyphicon-pencil"></span>
  </button>
</#if>