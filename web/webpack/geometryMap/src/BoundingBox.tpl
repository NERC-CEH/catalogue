<head>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.3.1/dist/leaflet.css" integrity="sha512-Rksm5RenBEKSKFjgI3a41vrjkw4EVPlJ3+OiI65vTjIdo9brlAacEuKOiQ5OFh7cOI1bkDwLqdLw3Zg0cRJAAQ==" crossorigin="" />
    <script src="https://unpkg.com/leaflet@1.3.1/dist/leaflet-src.js" integrity="sha512-IkGU/uDhB9u9F8k+2OsA6XXoowIhOuQL1NTgNZHY1nkURnqEGlDZq3GsfmdJdKFe1k1zOc6YU2K7qY+hF9AodA==" crossorigin=""></script>
    <link rel="stylesheet" href="https://unpkg.com/leaflet-draw@1.0.2/dist/leaflet.draw-src.css" />
    <script src="https://unpkg.com/leaflet-draw@1.0.2/dist/leaflet.draw-src.js"></script>
</head>
<div class="row">
    <div class="col-sm-2 col-lg-2">
        <label for="boundingBox<%= data.index %>WestBoundLongitude">West Bounding Longitude</label>
        <input data-name="westBoundLongitude" id="boundingBox<%= data.index %>WestBoundLongitude" class="editor-input" value="<%= data.westBoundLongitude %>">
    </div>
    <div class="col-sm-2 col-lg-2">
        <label for="boundingBox<%= data.index %>NorthBoundLatitude">North Bounding Latitude</label>
        <input data-name="northBoundLatitude" id="boundingBox<%= data.index %>NorthBoundLatitude" class="editor-input" value="<%= data.northBoundLatitude %>">
        <label for="boundingBox<%= data.index %>SouthBoundLatitude">South Bounding Latitude</label>
        <input data-name="southBoundLatitude" id="boundingBox<%= data.index %>SouthBoundLatitude" class="editor-input" value="<%= data.southBoundLatitude %>">
    </div>
    <div class="col-sm-2 col-lg-2">
        <label for="boundingBox<%= data.index %>EastBoundLongitude">East Bounding Longitude</label>
        <input data-name="eastBoundLongitude" id="boundingBox<%= data.index %>EastBoundLongitude" class="editor-input" value="<%= data.eastBoundLongitude %>">
        <br>
        <button class="editor-button" title="Show/Update map"><span class="fas fa-globe" aria-hidden="true"></span></button>
    </div>
    <div class="col-sm-6 col-lg-6">
        <div class="row">
            <div class="map" style="width: 500px; height: 500px;"></div>
        </div>
        <div class="row">
            <span class="extentName"><%= data.extentName %></span>
            <span class="extentUri"><%= data.extentUri %></span>
        </div>
    </div>
</div>
