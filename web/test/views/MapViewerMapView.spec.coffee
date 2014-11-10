define [
  'jquery'
  'underscore'
  'backbone'
  'cs!views/MapViewerMapView'
  'cs!collections/Layers'
  'cs!models/Layer'
], ($, _, Backbone, MapViewerMapView, Layers, Layer) ->
  describe "MapViewerMapView", ->
    el = null
    view = null
    layers = null
      
    beforeEach ->
      el = $('<div></div>').appendTo $('body')
      layers = new Layers
      view = new MapViewerMapView el: el, collection: layers

    afterEach ->
      do el.remove
    
    it "creates a tms layer when a layer is added", ->
      layer = new Layer
      layers.add layer

      expect(view.map.layers).toContain layer._openlayersTMS
      expect(layer._openlayersTMS).toBeTruthy()

    it "removes the tms layer when removed", ->
      layer = new Layer
      layers.add layer
      layers.remove layer

      expect(view.map.layers.length).toBe 1   # new layer and baselayer