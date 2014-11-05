define [
  'jquery'
  'cs!views/OpenLayersView'
], ($, OpenLayersView) ->
  describe "OpenLayersView", ->
    el = null

    beforeEach ->
      el = $('<div></div>').appendTo $('body')

    afterEach ->
      do el.remove
    
    it "defines an openlayers map", ->
      view = new OpenLayersView el: el
      expect(view.map).toBeDefined()

    it "contains a single background layer", ->
      view = new OpenLayersView el: el
      expect(view.map.layers.length).toBe 1

    it "can refresh the size and extent of the map", ->
      view = new OpenLayersView el: el
      spyOn(view.map, 'updateSize')
      spyOn(view.map, 'zoomToExtent')

      do view.refresh

      expect(view.map.updateSize).toHaveBeenCalled()
      expect(view.map.zoomToExtent).toHaveBeenCalled()