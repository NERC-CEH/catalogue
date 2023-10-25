<#--
  This freemarker fragment generates the static view of the drawing control.
  This will either be a button (for toggling drawing mode) or a anchor (for 
  removing a bbox filter)

  IMPORTANT: If you change the structure of this, please update the corresponding 
  javascript template /web/src/scripts/templates/DrawingControl.tpl
-->

  <div class="map"></div>

<#--<#if withoutBbox?has_content>-->
<#--  <div class="btn-group">-->
<#--    <button id="spatial-op-dropdown" class="btn btn-default btn-xs dropdown-toggle" type="button" data-toggle="dropdown">-->
<#--      <#if withinBbox?has_content>-->
<#--        Overlapping-->
<#--      <#else>-->
<#--        Entirely Within-->
<#--      </#if>-->
<#--      <span class="caret"></span>-->
<#--    </button>-->
<#--    <ul class="dropdown-menu" role="menu">-->
<#--      <li role="presentation"><a role="menuitem" tabindex="-1" href="${withinBbox!url}">Entirely Within</a></li>-->
<#--      <li role="presentation"><a role="menuitem" tabindex="-1" href="${intersectingBbox!url}">Overlapping</a></li>-->
<#--    </ul>-->
<#--  </div>-->

<#--  <a href="${withoutBbox}" class="btn btn-primary btn-xs">-->
<#--    Spatial Filter -->
<#--    <span class="fa-solid fa-xmark"></span>-->
<#--  </a>-->
<#--<#else>-->
<#--  <button id="drawing-toggle" type="button" class="btn btn-default btn-xs" aria-label="Draw a boundary in which to search">-->
<#--    <span class="fa-solid fa-pencil-alt"></span>-->
<#--  </button>-->
<#--</#if>-->