define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend

  urlRoot: '/documents'

  initialize: ->
    @mediaType = arguments[1].mediaType

  sync: (method, model, options)->
    Backbone.sync.call @, method, model, _.extend options,
      accepts:
        json: [@mediaType, "application/json"]
      contentType: @mediaType

  validate: (attrs) ->
    errors = []
    unless attrs?.title?
      errors.push 'Title required.'

    if _.isEmpty errors
      # return nothing from Backbone.Model.validate because returning something signals a validation error.
      return
    else
      return errors