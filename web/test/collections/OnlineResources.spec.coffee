define [
  'cs!collections/OnlineResources'
  'cs!models/OnlineResource'
], (OnlineResources, OnlineResource) ->
  describe "OnlineResources Collection", ->
    it "can filter online resources to only those which are wms", ->
      metadataDocument = id: 10
      resource1 = type: 'WMS_GET_CAPABILITIES'
      resource2 = type: 'OTHER'

      onlineResources = new OnlineResources [], metadataDocument: metadataDocument
      onlineResources.add [resource1, resource2]

      expect(onlineResources.getWmsResources().length).toBe 1