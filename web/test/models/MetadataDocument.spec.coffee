define [
  'cs!models/MetadataDocument'
  'cs!models/OnlineResource'
], (MetadataDocument, OnlineResource) ->
  describe "MetadataDocument", ->
    metadoc = null
    
    beforeEach ->
      metadoc = new MetadataDocument

    it "populates the online resources collection when the model changes", ->
      metadoc.set 'onlineResources', [type: 'anything']

      resources = metadoc.getOnlineResources()

      expect(resources.length).toBe 1
      expect(resources.at(0).get 'type').toBe 'anything'

    it "fetches the online resources which represent a wms", ->
      wms = new OnlineResource
      wms.isWms =-> true

      other = new OnlineResource
      other.isWms =-> false

      spyOn wms, 'fetch'
      spyOn other, 'fetch'

      metadoc.onlineResources.reset = jasmine.createSpy 'reset'
      metadoc.onlineResources.add [wms, other]

      do metadoc.populateOnlineResources

      expect(wms.fetch).toHaveBeenCalled()
      expect(other.fetch).not.toHaveBeenCalled()

    it "can return the onlineResources", ->
      metadoc.onlineResources = 'Some random onlineResources'

      resources = metadoc.getOnlineResources()

      expect(resources).toBe 'Some random onlineResources'

    it "proxies the onlineResources events", ->
      callback = jasmine.createSpy 'callback'
      metadoc.on 'resources-random', callback

      metadoc.onlineResources.trigger 'random'

      expect(callback).toHaveBeenCalled()