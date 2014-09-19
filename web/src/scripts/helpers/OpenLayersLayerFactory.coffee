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