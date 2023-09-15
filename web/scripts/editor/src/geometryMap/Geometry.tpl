<head>
    <link rel="stylesheet" href="https://unpkg.com/leaflet-draw@1.0.2/dist/leaflet.draw-src.css" />
</head>
<div class="row">
    <div class="col-sm-2">
        <button id="update" class="editor-button" title="Show/Update map"><span class="fa-solid fa-globe" aria-hidden="true"></span></button>
    </div>
    <div class="col-sm-6">
        <div class="row">
            <div class="map" style="width: 500px; height: 500px;"></div>
        </div>
    </div>
</div>
<br>
<div class="row">
    <div class="col-sm-2">
        <label>Advanced: Edit Geometry Json</label>
        <button class="editor-button-xs showhide" title="show/hide details"><span class="fa-solid fa-chevron-down" aria-hidden="true"></span></button>
        <textarea rows="20" data-name="geometryString" id="box" class="editor-input hidden" value="<%= data.geometryString %>" style="width: 500px; height: 250px;"><%= data.geometryString %></textarea>
        <br>
    </div>
    <br>
</div>
