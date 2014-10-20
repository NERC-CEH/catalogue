<#--
  This freemarker fragment generates the static view of the drawing control.
  This will either be a button (for toggling drawing mode) or a anchor (for 
  removing a bbox filter)

  IMPORTANT: If you change the structure of this, please update the corresponding 
  javascript template /web/src/scripts/templates/DrawingControl.tpl
-->
<#if withoutBBox?has_content>
  <a href="${withoutBBox}" class="btn btn-primary btn-xs">
    Spatial Filter 
    <span class="glyphicon glyphicon-remove"></span>
  </a>
<#else>
  <button type="button" class="btn btn-default btn-xs">
    <span class="glyphicon glyphicon-pencil"></span>
  </button>
</#if>