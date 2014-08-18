define [
  'cs!models/MetadataDocument'
], (MetadataDocument) ->
  describe "MetadataDocument", ->
    it "populates the online resources collection when the model changes", ->
      metadataDocument = new MetadataDocument
      metadataDocument.set 'onlineResources', [type: 'anything']

      resources = metadataDocument.getOnlineResources()

      expect(resources.length).toBe 1
      expect(resources.at(0).get 'type').toBe 'anything'