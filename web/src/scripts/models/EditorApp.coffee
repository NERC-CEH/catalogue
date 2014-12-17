define [
  'backbone'
  'cs!models/Metadata'
], (Backbone, Metadata) -> Backbone.Model.extend

  initialize: ->
    @set 'metadata', new Metadata()

    @listenTo @get 'metadata', 'error', ->
      console.log 'heard error'

  loadDocument: (identifier) ->
    metadata = @get('metadata')
    metadata.set 'id', identifier
    metadata.fetch
      success: (model) =>
        console.log "Success loading: #{model.id}"
        @trigger 'loaded'
      error: (model) =>
        console.log "Error loading: #{model.id}"
        @trigger 'error', "Unable to load metadata for: #{model.id}"

  newDocument: ->
    @trigger 'loaded'