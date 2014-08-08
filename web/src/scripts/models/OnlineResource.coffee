define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend

  url: -> '/documents/#{@metadataId}/onlineResources/#{@id}'

  initialize:->
    #Grab the metadata id off the metadata document. Store for easy access
    @metadataId = @collection.metadataDocument.id

  ###
  Determing if this online resource represents an WMS online resource.
  Do this by checking if the type is Get Capabilities
  ###
  isWms:-> @get('type') is 'GET_CAPABILITIES'

  ###
  Generate a url to the wms endpoint for this online resource. The url will
  only be valid if this online resource #isWms
  ###
  getWms:-> "#{@url()}/wms"