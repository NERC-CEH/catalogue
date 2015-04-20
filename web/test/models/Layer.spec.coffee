define [
  'cs!models/Layer'
], (Layer) ->
  describe "Layer", ->
    it "can hide open infos when layers info becomes visible", ->
      collection = hideLayerInfo:->
      spyOn(collection, 'hideLayerInfo');

      layer = new Layer
      layer.collection = collection # set the collection 

      layer.setInfoVisibility true
      expect(collection.hideLayerInfo).toHaveBeenCalled()