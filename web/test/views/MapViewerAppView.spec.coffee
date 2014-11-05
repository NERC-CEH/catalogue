define [
  'jquery'
  'cs!views/MapViewerAppView'
  'cs!models/MapViewerApp'
], ($, MapViewerAppView, MapViewerApp) ->
  describe "MapViewerAppView", ->
    el = null
    model = null

    beforeEach ->
      el = $('<div></div>').appendTo $('body')
      model = new MapViewerApp

    afterEach ->
      do el.remove
    
    it "creates mapviewer map view", ->
      view = new MapViewerAppView el: el, model: model

      expect(view.mapViewerMapView).toBeDefined()
      expect(view.mapViewerMapView.model).toBe model

    it "creates layersView map view", ->
      view = new MapViewerAppView el: el, model: model

      expect(view.layersView).toBeDefined()
      expect(view.layersView.collection).toBe model.getLayers()