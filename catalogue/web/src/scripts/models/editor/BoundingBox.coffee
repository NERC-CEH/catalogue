define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend {

  validate: (attrs) ->

    console.log attrs
    errors = []

    _.chain(attrs)
      .keys()
      .each (key) ->
        # coerce attribute to be a number with + then check if operation produced NaN
        if isNaN(+ attrs[key])
          errors.push message: "#{key} needs to be a number"


    if _.isNumber attrs.westBoundLongitude and _.isNumber attrs.eastBoundLongitude and attrs.westBoundLongitude > attrs.eastBoundLongitude
      errors.push message: "westBoundLongitude should be less the eastBoundLongitude"

    console.log errors
    console.log @
    if _.isEmpty errors
      # return nothing from Backbone.Model.validate because returning something signals a validation error.
      return
    else
      return errors
}
