define [
  'cs!collections/Layers'
  'cs!models/OnlineResourceLayer'
], (Layers, OnlineResourceLayer) ->
  describe "Layers Collection", ->
    it "hiding info hides underlying layers info", ->
      layers = new Layers
      layer = new OnlineResourceLayer infoVisible: true
      layers.add [layer]
      
      do layers.hideLayerInfo

      expect(layer.get 'infoVisible').toBe false