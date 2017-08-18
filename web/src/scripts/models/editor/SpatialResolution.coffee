define [
  'underscore'
  'backbone'
], (_, Backbone) -> Backbone.Model.extend {

  validate: (attrs) ->

    errors = []

    equivalentScale = attrs.equivalentScale
    distance = attrs.distance
    uom = attrs.uom

    if equivalentScale && (distance || uom)
      errors.push
        message: 'You should have either an Equivalent scale OR a Distance with UOM'

    if _.isEmpty errors
      # return nothing from Backbone.Model.validate
      # because returning something signals a validation error.
      return
    else
      return errors
}
