define [
  'underscore'
  'backbone'
  'node-uuid'
  'cs!models/Metadata'
], (_, Backbone, uuid, Metadata) -> Backbone.Model.extend

  loadDocument: (identifier) ->
    metadata = new Metadata
      id: identifier

    metadata.fetch
      success: (model) =>
        console.log "Success loading: #{model.id}"
        @set 'metadata', model
        @trigger 'loaded'
      error: (model) =>
        console.log "Error loading: #{model.id}"
        @trigger 'error', "Unable to load metadata for: #{model.id}"

  newDocument: ->
    @set 'metadata', new Metadata
      id: uuid.v4()
    @trigger 'loaded'

  getMetadata: ->
    _.clone(@get 'metadata')