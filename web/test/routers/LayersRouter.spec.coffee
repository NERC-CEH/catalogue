define [
  'cs!routers/LayersRouter'
], (LayersRouter) ->
  describe "LayersRouter", ->
    it "can parse the layer route of metadata ids", ->
      model = jasmine.createSpyObj 'model', ['set']

      router = new LayersRouter model: model
      router.loadLayers 'a!b!c'

      expect(model.set).toHaveBeenCalledWith 'metadataIds', ['a', 'b', 'c']
      