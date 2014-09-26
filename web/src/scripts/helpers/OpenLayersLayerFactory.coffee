define [
  'openlayers'
], (OpenLayers) ->
  ###
  Create an openlayers layer given some model/Layer which updates when different parts
  of the layer change
  ###
  createLayer: (layer) -> 
    tmsLayer = new OpenLayers.Layer.TMS layer.getName(), layer.getTMS(),
        layername: layer.getName()
        type: 'png'
        isBaseLayer: false
        opacity: layer.getOpacity()
        visibility: layer.isVisible()

    layer.on 'change:opacity', -> tmsLayer.setOpacity layer.getOpacity()
    layer.on 'change:visibility', -> tmsLayer.setVisibility layer.isVisible()
    return tmsLayer

  ###
  Create a drawing layer which represents the currently displayed search results
  ###
  createSearchResultsLayers: (searchResults) ->
    wktFactory = new OpenLayers.Format.WKT #Create the wktFactory to convert openlayers features to wkt
    drawingLayer = new OpenLayers.Layer.Vector "Vector Layer"
    markerLayer = new OpenLayers.Layer.Markers "Marker Layer"

    epsg4326 = new OpenLayers.Projection("EPSG:4326")

    updateDrawingLayer =->
      do drawingLayer.removeAllFeatures
      do markerLayer.clearMarkers

      _.forEach searchResults.getResultsOnScreen(), (result) ->
        vector = wktFactory.read result.getLocations()
        vector.geometry.transform epsg4326, drawingLayer.map.getProjectionObject()
        vector.style =
          strokeColor: '#8fca89' 
          fillColor: '#8fca89'
          fillOpacity: 0.3

        console.log vector
        drawingLayer.addFeatures vector
        markerLayer.addMarker new OpenLayers.Marker( new OpenLayers.LonLat(
          vector.geometry.bounds.right,
          vector.geometry.bounds.top
        ), new OpenLayers.Icon('/static/img/marker.png', {h:34, w:21}, {x:-10,y:-34}))


    searchResults.onscreen.on 'change', updateDrawingLayer
    return [drawingLayer, markerLayer]