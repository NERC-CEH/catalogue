define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend {


  validate: (attrs) ->

    errors = []

    equivalentScale = attrs.equivalentScale
    distance = attrs.distance

    if equivalentScale && distance
      errors.push
        message: "You can <b>EITHER</b> enter an Equivalent scale <b>OR</b> a Distance but not both."

    if _.isEmpty errors
      # return nothing from Backbone.Model.validate
      # because returning something signals a validation error.
      return
    else
      return errors
}
