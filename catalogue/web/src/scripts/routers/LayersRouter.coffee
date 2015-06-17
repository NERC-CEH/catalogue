define [
  'jquery'
  'underscore'
  'backbone'
], ($, _, Backbone) -> Backbone.Router.extend
  routes:
    "layers/:layers" : "loadLayers"

  initialize: (options) ->
    @model = options.model

  ###
  Grab the list of ids from the route and set these on the app model.
  This will trigger the app to fetch the relevant representations
  ###
  loadLayers: (route) ->
    @model.set 'metadataIds', route.split('!')
    console.log "Layers Router #{route}"