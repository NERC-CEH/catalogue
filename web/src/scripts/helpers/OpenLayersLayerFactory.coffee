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
  createSearchResultsLayer: (searchResults) ->
    wktFactory = new OpenLayers.Format.WKT #Create the wktFactory to convert openlayers features to wkt
    drawingLayer = new OpenLayers.Layer.Vector "Vector Layer",
      style:
        strokeColor: '#ff0000'
        fillOpacity: 0

    epsg4326 = new OpenLayers.Projection("EPSG:4326")

    updateDrawingLayer =->
      do drawingLayer.removeAllFeatures

      _.forEach searchResults.getResultsOnScreen(), (result) ->
        vector = wktFactory.read result.getLocations()
        vector.geometry.transform epsg4326, drawingLayer.map.getProjectionObject()
        drawingLayer.addFeatures vector

    searchResults.onscreen.on 'change', updateDrawingLayer
    return drawingLayer