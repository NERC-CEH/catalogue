define [
  'cs!models/OnlineResourceLayer'
  'cs!models/OnlineResource'
], (OnlineResourceLayer, OnlineResource) ->
  layer = null
  resource = null

  beforeEach ->
    resource = new OnlineResource
    layer = new OnlineResourceLayer onlineResource: resource

  describe "OnlineResourceLayer", ->
    it "can generate a legend url", ->
      layer.getLayer = -> 'layer'
      resource.url = -> 'somewhere'

      expect(layer.getLegend()).toBe 'somewhere/layer/legend'

    it "can generate a tms endpoint", ->
      resource.url = -> 'somewhere'

      expect(layer.getTMS()).toBe 'somewhere/tms/'