define [
  'cs!collections/Layers'
  'cs!models/OnlineResourceLayer'
], (Layers, OnlineResourceLayer) ->
  describe "Layers Collection", ->
    layers = null

    beforeEach ->
      layers = new Layers

    it "hiding info hides underlying layers info", ->
      layer = new OnlineResourceLayer infoVisible: true
      layers.add [layer]
      
      do layers.hideLayerInfo

      expect(layer.get 'infoVisible').toBe false

    it "can reposition layers which have already been added", ->
      layer1 = new OnlineResourceLayer {}
      layer2 = new OnlineResourceLayer {}
      layers.add [layer1, layer2]

      layers.position 0, 1

      expect(layers.at(1)).toBe layer1
      expect(layers.at(0)).toBe layer2