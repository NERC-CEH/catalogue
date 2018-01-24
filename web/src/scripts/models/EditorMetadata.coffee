define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend

  url: ->
    do @urlRoot

  urlRoot: ->
    if @isNew()
      "/documents?catalogue=#{window.location.pathname.split('/')[1]}"
    else
      "/documents/#{@id}"

  initialize: ->
    if arguments.length > 1
      @mediaType = arguments[1].mediaType
    else
      @mediaType = 'application/json'

  sync: (method, model, options)->
    Backbone.sync.call @, method, model, _.extend options,
      accepts:
        json: @mediaType
      contentType: @mediaType

  validate: (attrs) ->
    errors = []
    unless attrs?.title?
      errors.push 'A title is mandatory'

    if _.isEmpty errors
      # return nothing from Backbone.Model.validate because returning something signals a validation error.
      return
    else
      return errors