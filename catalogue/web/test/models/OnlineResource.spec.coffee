define [
  'cs!models/OnlineResource'
  'cs!models/MetadataDocument'
], (OnlineResource, MetadataDocument) ->
  describe "OnlineResource", ->
    resource = null
    
    beforeEach ->
      resource = new OnlineResource

    it "can decide if represents a wms", ->
      resource.set 'type', 'WMS_GET_CAPABILITIES'

      expect(resource.isWms()).toBe true

    it "can return the layers with online resource attached", ->
      resource.set 'layers', [name: 'myName' ]

      expect(resource.getLayers()).toEqual [
        name: 'myName', onlineResource: resource
      ]

    it "can define its url location", ->
      doc = new MetadataDocument {id: 'myDocId' }
      doc.onlineResources.add resource
      resource.set 'id', 40
      
      expect(resource.url()).toBe '/documents/myDocId/onlineResources/40'