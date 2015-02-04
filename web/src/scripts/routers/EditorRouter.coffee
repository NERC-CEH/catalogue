define [
  'backbone'
], (Backbone) -> Backbone.Router.extend

  routes:
    'edit/new': 'newRecord'
    'edit/:identifier' : 'loadRecord'

  initialize: (options) ->
    @model = options.model

  loadRecord: (identifier) ->
    console.log "loading record for: #{identifier}"
    @model.loadDocument identifier

  newRecord: ->
    console.log 'new record'
    do @model.newDocument