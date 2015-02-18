define [
  'backbone'
], (Backbone) -> Backbone.Model.extend

  url: ->
    do @urlRoot

  urlRoot: ->
    "/documents/#{@id}/permission"