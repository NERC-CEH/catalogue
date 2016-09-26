define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend

  url: ->
    do @urlRoot

  urlRoot: ->
    "/documents/#{@id}/catalogue"
