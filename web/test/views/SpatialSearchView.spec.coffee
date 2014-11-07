define [
  'jquery'
  'openlayers'
  'cs!views/SpatialSearchView'
  'cs!models/SearchApp'
], ($, OpenLayers, SpatialSearchView, SearchApp) ->
  describe "SpatialSearchView", ->
    el = null
    model = null
    view = null

    beforeEach ->
      el = $('<div></div>').appendTo $('body')
      model = new SearchApp
      view = new SpatialSearchView el: el, model: model

    afterEach ->
      do el.remove
    
    it "enables drawing control when in drawing mode", ->
      model.set drawing: true
      expect(view.drawingControl.active).toBeTruthy()

    it "disables drawing control when not drawing", ->
      model.set drawing: false    
      expect(view.drawingControl.active).not.toBeTruthy()

    it "can set the defined bounding box on the drawing layer", ->
      model.set bbox: '-180,-90,180,90'
      expect(view.drawingLayer.features.length).toBe 1

    it "can remove the defined bounding box on the drawing layer", ->
      model.set bbox: '-180,-90,180,90'
      model.unset 'bbox'
      expect(view.drawingLayer.features.length).toBe 0

    it "sets drawn features onto the model", ->
      feature = new OpenLayers.Feature.Vector OpenLayers.Geometry.fromWKT '''
        POLYGON((-20037508.34 -20037508.34,\
                 -20037508.34 20037508.34,\
                 20037508.34 20037508.34,\
                 20037508.34 -20037508.34,\
                 -20037508.34 -20037508.34))'''
      view.drawingLayer.addFeatures feature

      expect(model.has 'bbox').toBe true