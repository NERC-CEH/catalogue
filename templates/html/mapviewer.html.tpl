<#import "skeleton.html.tpl" as skeleton>
<@skeleton.master title="Map Viewer">
  <div id="mapviewer" >
    <div class="openlayers" style="position:absolute; top:0; bottom:0; left:0; right:0;"></div>
    <div class="layers">
      <ul class="list-group">
      </ul>
    </div>
  </div>
  <script data-main="static/scripts/main-out" src="/static/vendor/requirejs/require.js"></script>
</@skeleton.master>