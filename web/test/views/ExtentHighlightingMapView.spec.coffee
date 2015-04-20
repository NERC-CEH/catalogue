define [
  'jquery'
  'underscore'
  'backbone'
  'cs!views/ExtentHighlightingMapView'
], ($, _, Backbone, ExtentHighlightingMapView) ->
  describe "ExtentHighlightingMapView", ->
    el = null
    view = null
      
    beforeEach ->
      el = $('<div></div>').appendTo $('body')
      view = new ExtentHighlightingMapView el: el

    afterEach ->
      do el.remove
    
    describe "Filter Function", ->
      filter = null

      beforeEach ->
        filter = view.createFilterFunction()
      
      it "shows points when length not visible", ->
        view.isLengthVisible=-> false

        expect(filter.evaluate isPoint: true).toBe true
        expect(filter.evaluate isPoint: false).toBe false

      it "hides points when length is visible", ->
        view.isLengthVisible=-> true

        expect(filter.evaluate isPoint: true).toBe false
        expect(filter.evaluate isPoint: false).toBe true

    it "can calculate if length is invisible", ->
      view.minGeoLength = 20
      view.map.getResolution=-> 2

      expect(view.isLengthVisible 10).toBe false

    it "can calculate if length is visible", ->
      view.minGeoLength = 20
      view.map.getResolution=-> 2

      expect(view.isLengthVisible 100).toBe true

    it "can zoom to the highlighted area", ->
      spyOn(view.map, 'zoomToExtent')
      spyOn(view.highlightedLayer, 'getDataExtent').and.returnValue 'extent'

      do view.zoomToHighlighted

      expect(view.highlightedLayer.getDataExtent).toHaveBeenCalled()
      expect(view.map.zoomToExtent).toHaveBeenCalledWith 'extent'

    it "delegates to setHighlighted when using boxes", ->
      spyOn(view, 'setHighlighted')

      view.setHighlightedBoxes ['-180 -90 180 80']

      expect(view.setHighlighted).toHaveBeenCalledWith [
        'POLYGON((-180 -90, -180 80, 180 80, 180 -90, -180 -90))'
      ]

    it "can transform a location string into a wkt representation", ->
      wkt = view.bbox2WKT '-180 -90 180 90'
      expect(wkt).toBe 'POLYGON((-180 -90, -180 90, 180 90, 180 -90, -180 -90))'

    it "can read wkt to openlayers feature", ->
      vector = view.readWKT 'POLYGON((-180 -90, -180 90, 180 90, 180 -90, -180 -90))'

      expect( vector.geometry.toString()).toBe '''
        POLYGON((-20037508.34 -20037508.34,\
                 -20037508.34 20037508.34,\
                 20037508.34 20037508.34,\
                 20037508.34 -20037508.34,\
                 -20037508.34 -20037508.34))'''

    describe "highlighted layer", ->
      it "contains point and vector when location set", ->
        wkt = 'POLYGON((-180 -90, -180 90, 180 90, 180 -90, -180 -90))'
        view.setHighlighted [wkt]
        features = view.highlightedLayer.features

        expect(view.highlightedLayer.features.length).toBe 2
        expect(_.find(features, (f)-> f.attributes.isPoint)).toBeDefined()
        expect(_.find(features, (f)-> not f.attributes.isPoint)).toBeDefined()

      it "has features with areaRootDefined", ->
        wkt = 'POLYGON((-180 -90, -180 90, 180 90, 180 -90, -180 -90))'
        view.setHighlighted [wkt]
        features = view.highlightedLayer.features

        expect(_.every features, (f) -> f.attributes.areaRoot).toBe true

      it "is added as a layer on the map", ->
        expect(view.map.layers).toContain view.highlightedLayer