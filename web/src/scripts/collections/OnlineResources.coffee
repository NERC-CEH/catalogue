define [
  'backbone'
  'cs!models/OnlineResource'
], (Backbone, OnlineResource) -> Backbone.Collection.extend
  model: OnlineResource

  ###
  Initialize a collection of Online Resources. Obviously online resources are 
  tied to a particular MetadataDocument. Therefore we require that metadata 
  document to be supplied in the options.
  ###
  initialize: (models, options) ->
    @metadataDocument = options.metadataDocument

  ###
  Filter the collection to only online resources which are wms resources
  ###
  getWmsResources:-> @filter (onlineResource) -> onlineResource.isWms()