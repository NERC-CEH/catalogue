define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend 

  validate: (attrs) ->

    errors = []

    name = attrs.name
    type = attrs.type
    version = attrs.version

    if ! version && (name || type) 
      errors.push
        message: "The version is mandatory - if it's not applicable, enter 'unknown'"

    if _.isEmpty errors
      # return nothing from Backbone.Model.validate
      # because returning something signals a validation error.
      return
    else
      return errors

