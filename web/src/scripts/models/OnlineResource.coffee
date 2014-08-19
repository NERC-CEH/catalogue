define [
  'underscore'
  'backbone'
  'cs!collections/Layers'
], (_, Backbone, Layers) -> Backbone.Model.extend

  url: -> "/documents/#{@metadataId}/onlineResources/#{@id}"

  initialize:->
    #Grab the metadata id off the metadata document. Store for easy access
    @metadataId = @collection.metadataDocument.id

  ###
  Determing if this online resource represents an WMS online resource.
  Do this by checking if the type is Get Capabilities
  ###
  isWms:-> @get('type') is 'GET_CAPABILITIES'

  ###
  Generate a Layers collection for each of the layers which this OnlineResource
  can render
  ###
  getLayers:-> _.map @attributes.layers, (layer) => 
    _.extend layer, onlineResource: @