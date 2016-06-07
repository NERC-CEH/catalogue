define [
  'underscore'
  'backbone'
  'cs!collections/Layers'
], (_, Backbone, Layers) -> Backbone.Model.extend

  ###
  The url for this OnlineResource is dynamic. It is only valid if this model
  has been attached to an OnlineResources collection
  ###
  url:-> "/documents/#{@collection.metadataDocument.id}/onlineResources/#{@id}"

  ###
  Determing if this online resource represents an WMS online resource.
  Do this by checking if the type is Get Capabilities
  ###
  isWms:-> @get('type') is 'WMS_GET_CAPABILITIES'

  ###
  Generate a Layers collection for each of the layers which this OnlineResource
  can render
  ###
  getLayers:-> _.map @attributes.layers, (layer) => 
    _.extend layer, onlineResource: @