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
        message: 'Should not have Distance or UOM with Equivalent Scale'

    if _.isEmpty errors
      # return nothing from Backbone.Model.validate
      # because returning something signals a validation error.
      return
    else
      return errors
}
