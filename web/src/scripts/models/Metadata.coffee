define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend
  urlRoot: '/documents'

  sync: (method, model, options)->
    Backbone.sync.call @, method, model, _.extend options,
      accepts:
        json: ["application/gemini+json", "application/json"]
      contentType: "application/gemini+json"