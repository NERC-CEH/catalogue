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

  validate: (attrs) ->
    errors = []
    unless attrs?.title?
      errors.push 'Title required.'

    if _.isEmpty errors
      # return nothing from Backbone.Model.validate because returning something signals a validation error.
      return
    else
      return errors