define [
  'openlayers'
], (OpenLayers) ->
  ###
  Create an openlayers layer given some model/Layer which updates when different parts
  of the layer change
  ###
  createLayer: (layer) -> 
    wmsLayer = new OpenLayers.Layer.WMS layer.getName(), layer.getWMS(), 
        layers: [layer.getLayer()]
        format:"image/png"
        transparent: true
      ,
        isBaseLayer:false
        opacity: layer.getOpacity()
        visibility: layer.isVisible()
        projection: new OpenLayers.Projection "EPSG:102100"

    layer.on 'change:opacity', -> wmsLayer.setOpacity layer.getOpacity()
    layer.on 'change:visibility', -> wmsLayer.setVisibility layer.isVisible()
    return wmsLayer