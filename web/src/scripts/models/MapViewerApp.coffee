define [
  'underscore'
  'backbone'
  'cs!collections/MetadataDocuments'
  'cs!collections/Layers'
], (_, Backbone, MetadataDocuments, Layers) -> Backbone.Model.extend

  initialize:->
    @metadataDocuments = new MetadataDocuments()
    @layers = new Layers

    @on 'change:metadataIds', @fetchMetadataDocuments

    _.bindAll this, 'handledSyncedResource', 'handleError'
    
    @metadataDocuments.on 'error',           @handleError
    @metadataDocuments.on 'resources-error', @handleError
    @metadataDocuments.on 'resources-sync',  @handledSyncedResource


  ###
  Listens to when an online resource has been synced. This is the case when
  that online resource represents a wms get capabilities. Since we now have it
  get the list of layers which it specifies to our layers collection
  ###
  handledSyncedResource: (model) -> @layers.add model.getLayers()

  ###
  The list of metadata document ids has changes. Clear out the metadata 
  documents collection and populate with the new set
  ###
  fetchMetadataDocuments: ->
    do @layers.reset # Clear out any set layers which we may already have.
    @metadataDocuments.setById @get 'metadataIds'
    @metadataDocuments.forEach (doc) -> do doc.fetch

  ###
  Grab layers collection from this model
  ###
  getLayers: -> @layers

  ###
  Handle any sync errors from either a resource or metadata document. Flag an
  error event so that we can display this in the message panel.
  ###
  handleError: (args...) -> @trigger 'error', args...
